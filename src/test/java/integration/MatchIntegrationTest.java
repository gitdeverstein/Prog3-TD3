package integration;

import app.foot.FootApi;
import app.foot.controller.rest.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static utils.TestUtils.assertThrowsExceptionMessage;

@SpringBootTest(classes = FootApi.class)
@AutoConfigureMockMvc
@Slf4j
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();  //Allow 'java.time.Instant' mapping

    @Test
    void read_match_by_id_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches/2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(expectedMatch2(), actual);
    }

    @Test
    void read_matches_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        List<Match> actual = objectMapper.readValue(
                response.getContentAsString(), objectMapper.getTypeFactory().constructCollectionType(List.class, Match.class));

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(3, actual.size());
    }

    @Test
    void add_goal_in_match_with_id3_ok() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/matches/3/goals")
                        .content(objectMapper.writeValueAsString(List.of(playerScorerMatchId3())))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        Match actual = objectMapper.readValue(
                response.getContentAsString(), Match.class);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(expectedMatch3(), actual);
    }
    @Test
    void add_goal_in_match_with_id3_ko() throws Exception {
        String Id = "3";
        String message = "400 BAD_REQUEST ABOUT PLAYER "+player7().getId();
        assertThrowsExceptionMessage(message, ServletException.class,
                ()->mockMvc.perform(post("/matches/"+Id+"/goals")
                                .content(objectMapper.writeValueAsString(List.of(playerScorerMatchId3().toBuilder().player(player7()).build())))
                                .contentType("application/json"))
                        .andExpect(status().isNotFound())
        );
    }

    private static Match expectedMatch3() {
        return Match.builder()
                .id(3)
                .teamA(teamMatchAId3())
                .teamB(teamMatchBId3())
                .stadium("S3")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static Match expectedMatch2() {
        return Match.builder()
                .id(2)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S2")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static Match expectedMatch1() {
        return Match.builder()
                .id(1)
                .teamA(teamMatchA())
                .teamB(teamMatchB())
                .stadium("S1")
                .datetime(Instant.parse("2023-01-01T14:00:00Z"))
                .build();
    }

    private static TeamMatch teamMatchB() {
        return TeamMatch.builder()
                .team(team3())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchA() {
        return TeamMatch.builder()
                .team(team2())
                .score(2)
                .scorers(List.of(PlayerScorer.builder()
                                .player(player3())
                                .scoreTime(70)
                                .isOG(false)
                                .build(),
                        PlayerScorer.builder()
                                .player(player6())
                                .scoreTime(80)
                                .isOG(true)
                                .build()))
                .build();
    }

    private static TeamMatch teamMatchAId3() {
        return TeamMatch.builder()
                .team(team4())
                .score(0)
                .scorers(List.of())
                .build();
    }

    private static TeamMatch teamMatchBId3() {
        return TeamMatch.builder()
                .team(team3())
                .score(1)
                .scorers(List.of())
                .build();
    }

    private static Team team4() {
        return Team.builder()
                .id(4)
                .name("E4")
                .build();
    }

    private static Team team3() {
        return Team.builder()
                .id(3)
                .name("E3")
                .build();
    }

    private static Player player6() {
        return Player.builder()
                .id(6)
                .name("J6")
                .teamName("E3")
                .isGuardian(false)
                .build();
    }

    private static Player player7() {
        return Player.builder()
                .id(7)
                .name("G3")
                .isGuardian(true)
                .teamName("E3")
                .build();
    }

    private static Player player3() {
        return Player.builder()
                .id(3)
                .name("J3")
                .teamName("E2")
                .isGuardian(false)
                .build();
    }

    private static Team team2() {
        return Team.builder()
                .id(2)
                .name("E2")
                .build();
    }

    private static PlayerScorer playerScorerMatchId3() {
        return PlayerScorer.builder()
                .player(player6())
                .scoreTime(34)
                .isOG(false)
                .build();
    }

    private List<Match> convertFromHttpResponse(MockHttpServletResponse response)
            throws JsonProcessingException, UnsupportedEncodingException {
        CollectionType playerListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Match.class);
        return objectMapper.readValue(
                response.getContentAsString(),
                playerListType);
    }
}
