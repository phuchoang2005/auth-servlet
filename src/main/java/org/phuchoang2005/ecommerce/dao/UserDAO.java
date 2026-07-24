package org.phuchoang2005.ecommerce.dao;

import org.phuchoang2005.ecommerce.dto.UserAuthDTO;
import org.phuchoang2005.ecommerce.enums.DuplicateFieldEnum;
import org.phuchoang2005.ecommerce.exceptions.database.DuplicateEntryDatabaseException;
import org.phuchoang2005.ecommerce.exceptions.database.QueryException;
import org.phuchoang2005.ecommerce.util.DBContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public Optional<UserAuthDTO> findAuthInfoByUsername(String username){
        String sql = "SELECT user_id, password,role FROM users WHERE username = ?";
        Connection conn = DBContextUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            logger.info("[DAO]Successful created prepared statement");
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                logger.info("[DAO]Result set created successfully");
                if (rs.next()) {
                    logger.info("[DAO]Querying authentication Information");
                    String user_id = rs.getString("user_id");
                    logger.info("[DAO]Querying user_id successfully");
                    String role = rs.getString("role");
                    logger.info("[DAO]Querying role successfully");
                    String password = rs.getString("password");
                    logger.info("[DAO]Querying password successfully");
                    return Optional.of(new UserAuthDTO(
                            user_id,
                            role,
                            password
                    ));
                }
            }catch(SQLException e){
                throw new QueryException("Error when running ResultSet " + e.getMessage());
            }
        }catch (SQLException e){
            throw new QueryException("Error when trying to make prepared statement");
        }
        return Optional.empty();
    }
    public Optional<Integer> insertUser(String username, String password){
        Connection conn = DBContextUtil.getConnection();
        String sql = "insert into users (username, password) values (?,?)";

        try(PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
            logger.info("[DAO]Successful created prepared statement");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            try(ResultSet rs = ps.getGeneratedKeys()){
                if (rs.next()){
                    Optional<Integer> result = Optional.of(rs.getInt(1));
                    logger.info("[DAO] Insert username successfully");
                    return result;
                }
            }catch(SQLException e){
                throw new QueryException(e.getMessage());
            }
        }catch(SQLException e){
            // 23505 = H2 DUPLICATE_KEY_1 (unique constraint violation)
            if (e.getErrorCode() == 23505){
                DuplicateFieldEnum field = DuplicateFieldEnum.fromErrorMessage(e.getMessage());
                if (field != null){
                    throw new DuplicateEntryDatabaseException(field.getFriendlyName() + " existed");
                }
            }
        }
        return Optional.empty();
    }
}