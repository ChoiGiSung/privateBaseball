package com.codesquad.coco.game.domain.DAO;

import com.codesquad.coco.game.domain.model.Game;
import com.codesquad.coco.game.domain.model.ScoreBoard;
import com.codesquad.coco.player.domain.PlayerDAO;
import com.codesquad.coco.utils.mapper.HomeAwayTeamNameMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.codesquad.coco.utils.SQL.*;

@Component
public class GameDAO {

    private JdbcTemplate template;
    private PlayerDAO playerDAO;
    private ScoreBoardDAO boardDAO;
    private HomeAwayTeamNameMapper homeAwayTeamNameMapper = new HomeAwayTeamNameMapper();
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GameDAO(DataSource dataSource, PlayerDAO playerDAO, ScoreBoardDAO boardDAO, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.template = new JdbcTemplate(dataSource);
        this.playerDAO = playerDAO;
        this.boardDAO = boardDAO;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Long save(Game game) {
        Long gameId = saveGame(game);
        ScoreBoard away = new ScoreBoard(gameId, game.awayTeamName());
        ScoreBoard home = new ScoreBoard(gameId, game.homeTeamName());
        saveScoreBoard(game, away);
        saveScoreBoard(game, home);
        return gameId;
    }

    private Long saveScoreBoard(Game game, ScoreBoard away) {
        Long scoreId = boardDAO.save(away);
        game.addScoreBoard(away);
        return scoreId;
    }

    private Long saveGame(Game game) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("home", game.homeTeamName());
        parameter.addValue("away", game.awayTeamName());
        parameter.addValue("user_type", game.getUserType().toString());

        namedParameterJdbcTemplate.update(GAME_SAVE_SQL, parameter, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public String findUserTeamNameByGameId(Long id) {
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("id", id);
        return namedParameterJdbcTemplate.queryForObject(FIND_USER_TEAM_NAME_SQL, parameter, homeAwayTeamNameMapper);
    }

    public String findHomeTeamNameByGameId(Long id) {
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("id", id);
        return namedParameterJdbcTemplate.queryForObject(FIND_HOME_TEAM_NAME_BY_GAME_ID, parameter, homeAwayTeamNameMapper);
    }

    public String findAwayTeamNameByGameId(Long id) {
        MapSqlParameterSource parameter = new MapSqlParameterSource();
        parameter.addValue("id", id);
        return namedParameterJdbcTemplate.queryForObject(FIND_AWAY_TEAM_NAME_BY_GAME_ID, parameter, homeAwayTeamNameMapper);
    }
}
