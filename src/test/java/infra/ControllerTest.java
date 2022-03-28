package infra;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

/**
 * Classe base para testes de Controllers
 */
public abstract class ControllerTest {

    protected static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    private static final String APP_CONTEXT_PATH = "classpath:applicationContext-Test.xml";

    protected MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception {
        StandaloneMockMvcBuilder standaloneMockMvcBuilder = MockMvcBuilders.standaloneSetup(this.getController());
        if (this.getControllerAdvice() != null) {
            standaloneMockMvcBuilder.setControllerAdvice(this.getControllerAdvice());
        }
        standaloneMockMvcBuilder.addFilter((request, response, chain) -> {
            response.setCharacterEncoding("UTF-8");
            chain.doFilter(request, response);
        });
        this.mockMvc = standaloneMockMvcBuilder.build();
    }

    protected Object getControllerAdvice() {
        return null;
    }

    public abstract Object getController();

}