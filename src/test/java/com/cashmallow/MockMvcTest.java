package com.cashmallow;

import com.cashmallow.config.EnableDevLocal;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
// @WebMvcTest(ApiController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@EnableDevLocal
public class MockMvcTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Gson gson;

    public String accessToken = "";

    @BeforeAll
    public void accessToken() throws Exception {
        final MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
                        .get("/devoffice/contingency/user/token?email=tiger002@ruu.kr&password=tiger002!")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        accessToken = mvcResult.getResponse().getContentAsString();
        // .andExpect(MockMvcResultMatchers.jsonPath("$.employees").exists())
        // .andExpect(MockMvcResultMatchers.jsonPath("$.employees[*].employeeId").isNotEmpty());
    }


    @Test
    public void getAllEmployeesAPI() throws Exception {
        // mvc.perform( MockMvcRequestBuilders
        //                 .post("/employees")
        //                 .content(asJsonString(new EmployeeVO(null, "firstName4", "lastName4", "email4@mail.com")))
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .accept(MediaType.APPLICATION_JSON))
        //         .andExpect(status().isCreated())
        //         .andExpect(MockMvcResultMatchers.jsonPath("$.employeeId").exists());
        //
        // final String body = mvcResult.getResponse().getContentAsString();
        // log.info(body);
        // .andExpect(MockMvcResultMatchers.jsonPath("$.employees").exists())
        // .andExpect(MockMvcResultMatchers.jsonPath("$.employees[*].employeeId").isNotEmpty());
    }
}
