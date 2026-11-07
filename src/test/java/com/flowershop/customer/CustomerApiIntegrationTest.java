package com.flowershop.customer;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test tích hợp với Postgres thật (Testcontainers). Cần Docker đang chạy.
 * Các test dùng chung một DB nên mỗi khách hàng có SĐT riêng (nextPhone) để không đụng nhau.
 */
@SpringBootTest(properties = {
        "DB_URL=jdbc:postgresql://unused:5432/unused",
        "DB_USERNAME=unused",
        "DB_PASSWORD=unused",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers
class CustomerApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final AtomicInteger PHONE_SEQ = new AtomicInteger();

    @Autowired
    MockMvc mockMvc;

    // ---------- Khách hàng ----------

    @Test
    void create_returns201_withCustomerCode() throws Exception {
        String phone = nextPhone();
        mockMvc.perform(post("/api/v1/customers").contentType(APPLICATION_JSON)
                        .content(customerJson(phone, "Lan.Nguyen@Gmail.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("00"))
                .andExpect(jsonPath("$.data.customerCode").value(matchesPattern("^KH_CN_\\d{7}$")))
                .andExpect(jsonPath("$.data.phone").value(phone))
                .andExpect(jsonPath("$.data.email").value("lan.nguyen@gmail.com")) // đã chuẩn hóa chữ thường
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void create_duplicatePhone_returns409() throws Exception {
        String phone = nextPhone();
        createCustomer(phone);

        mockMvc.perform(post("/api/v1/customers").contentType(APPLICATION_JSON).content(customerJson(phone, null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUS-409-001"));
    }

    @Test
    void create_duplicateEmailDifferentCase_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(APPLICATION_JSON)
                        .content(customerJson(nextPhone(), "dup@flower.vn")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers").contentType(APPLICATION_JSON)
                        .content(customerJson(nextPhone(), "DUP@flower.vn")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUS-409-002"));
    }

    @Test
    void create_invalidPhone_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/customers").contentType(APPLICATION_JSON).content(customerJson("12345", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COM-400-001"))
                .andExpect(jsonPath("$.data[0].field").value("phone"));
    }

    @Test
    void update_withStaleVersion_returns409() throws Exception {
        long id = createCustomer(nextPhone());

        mockMvc.perform(put("/api/v1/customers/" + id).contentType(APPLICATION_JSON)
                        .content("""
                                {"fullName":"Tên mới","version":99}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COM-409-001"));
    }

    @Test
    void list_masksPhoneAndEmail() throws Exception {
        String phone = nextPhone();
        mockMvc.perform(post("/api/v1/customers").contentType(APPLICATION_JSON)
                        .content(customerJson(phone, "hoa@flower.vn")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/customers").param("phone", phone))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].maskedPhone").value("*******" + phone.substring(7)))
                .andExpect(jsonPath("$.data.items[0].maskedEmail").value("h***@flower.vn"))
                .andExpect(jsonPath("$.data.items[0].phone").doesNotExist());
    }

    // ---------- Sổ địa chỉ ----------

    @Test
    void firstAddress_becomesDefault() throws Exception {
        long customerId = createCustomer(nextPhone());

        mockMvc.perform(post(addressesUrl(customerId)).contentType(APPLICATION_JSON).content(addressJson("Nhà riêng")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.defaultAddress").value(true));

        mockMvc.perform(post(addressesUrl(customerId)).contentType(APPLICATION_JSON).content(addressJson("Công ty")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.defaultAddress").value(false));
    }

    @Test
    void setDefault_leavesExactlyOneDefault() throws Exception {
        long customerId = createCustomer(nextPhone());
        long first = createAddress(customerId, "Nhà riêng");
        long second = createAddress(customerId, "Công ty");

        mockMvc.perform(put(addressesUrl(customerId) + "/" + second + "/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultAddress").value(true));

        String body = listAddresses(customerId);
        List<Integer> defaultIds = JsonPath.read(body, "$.data[?(@.defaultAddress == true)].id");
        assertThat(defaultIds).containsExactly((int) second);
        assertThat(JsonPath.<Integer>read(body, "$.data[0].id")).isEqualTo((int) second); // mặc định đứng đầu
        assertThat(JsonPath.<List<Integer>>read(body, "$.data[*].id")).contains((int) first);
    }

    @Test
    void deleteDefault_promotesNewestRemaining() throws Exception {
        long customerId = createCustomer(nextPhone());
        long first = createAddress(customerId, "Nhà riêng");   // mặc định
        createAddress(customerId, "Công ty");
        long newest = createAddress(customerId, "Nhà bố mẹ");

        mockMvc.perform(delete(addressesUrl(customerId) + "/" + first))
                .andExpect(status().isOk());

        String body = listAddresses(customerId);
        List<Integer> defaultIds = JsonPath.read(body, "$.data[?(@.defaultAddress == true)].id");
        assertThat(defaultIds).containsExactly((int) newest);
        assertThat(JsonPath.<List<Integer>>read(body, "$.data[*].id")).hasSize(2);
    }

    @Test
    void eleventhAddress_returns422() throws Exception {
        long customerId = createCustomer(nextPhone());
        for (int i = 1; i <= 10; i++) {
            createAddress(customerId, "Địa chỉ " + i);
        }

        mockMvc.perform(post(addressesUrl(customerId)).contentType(APPLICATION_JSON).content(addressJson("Thứ 11")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CUS-422-002"));
    }

    @Test
    void addressOfAnotherCustomer_returns404() throws Exception {
        long owner = createCustomer(nextPhone());
        long other = createCustomer(nextPhone());
        long addressId = createAddress(owner, "Nhà riêng");

        mockMvc.perform(put(addressesUrl(other) + "/" + addressId + "/default"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUS-404-002"));
        mockMvc.perform(delete(addressesUrl(other) + "/" + addressId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUS-404-002"));
    }

    @Test
    void inactiveCustomer_cannotAddAddress() throws Exception {
        long customerId = createCustomer(nextPhone());
        mockMvc.perform(delete("/api/v1/customers/" + customerId)).andExpect(status().isOk());

        mockMvc.perform(post(addressesUrl(customerId)).contentType(APPLICATION_JSON).content(addressJson("Nhà riêng")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CUS-422-001"));
    }

    // ---------- Helpers ----------

    private static String nextPhone() {
        return "09%08d".formatted(PHONE_SEQ.incrementAndGet());
    }

    private static String addressesUrl(long customerId) {
        return "/api/v1/customers/" + customerId + "/addresses";
    }

    private static String customerJson(String phone, String email) {
        String emailField = email == null ? "" : ",\"email\":\"%s\"".formatted(email);
        return """
                {"fullName":"Nguyễn Thị Lan","phone":"%s","dateOfBirth":"1995-03-20"%s}
                """.formatted(phone, emailField);
    }

    private static String addressJson(String note) {
        return """
                {"recipientName":"Trần Văn Bình","recipientPhone":"0987654321",
                 "addressLine":"12 Lý Thường Kiệt","ward":"Phường Cửa Nam","province":"Hà Nội","note":"%s"}
                """.formatted(note);
    }

    private long createCustomer(String phone) throws Exception {
        String body = mockMvc.perform(post("/api/v1/customers").contentType(APPLICATION_JSON).content(customerJson(phone, null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Integer>read(body, "$.data.id");
    }

    private long createAddress(long customerId, String note) throws Exception {
        String body = mockMvc.perform(post(addressesUrl(customerId)).contentType(APPLICATION_JSON).content(addressJson(note)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Integer>read(body, "$.data.id");
    }

    private String listAddresses(long customerId) throws Exception {
        return mockMvc.perform(get(addressesUrl(customerId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}
