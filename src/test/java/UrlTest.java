import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.hateoas.UriTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author dave 5/18/14 12:54 AM
 */
public class UrlTest {

    @Test
    public void testUris(){
        String url = "/foo/{peId}";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        String result = builder.buildAndExpand(1L).toString();
        assertEquals( "/foo/1", result.toString());
    }
}
