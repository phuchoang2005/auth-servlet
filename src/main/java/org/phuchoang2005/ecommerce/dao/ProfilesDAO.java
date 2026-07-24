package org.phuchoang2005.ecommerce.dao;

import org.phuchoang2005.ecommerce.dto.RegisterRequestDTO;
import org.phuchoang2005.ecommerce.enums.DuplicateFieldEnum;
import org.phuchoang2005.ecommerce.exceptions.database.DuplicateEntryDatabaseException;
import org.phuchoang2005.ecommerce.util.DBContextUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfilesDAO {
    public void insertProfile(RegisterRequestDTO registerRequestDTO, int user_id){
        Connection conn = DBContextUtil.getConnection();
        String sql = "insert into profiles (user_id, full_name, email, phone, address) values (?,?,?,?,?)";

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, user_id);
            ps.setString(2, registerRequestDTO.getFullname());
            ps.setString(3, registerRequestDTO.getEmail());
            ps.setString(4, registerRequestDTO.getPhone());
            ps.setString(5, registerRequestDTO.getAddress());
            ps.executeUpdate();
        }catch(SQLException e){
            // 23505 = H2 DUPLICATE_KEY_1 (unique constraint violation)
            if (e.getErrorCode() == 23505){
                DuplicateFieldEnum field = DuplicateFieldEnum.fromErrorMessage(e.getMessage());
                if (field != null){
                    throw new DuplicateEntryDatabaseException(field.getFriendlyName() + " existed");
                }
            }else{
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
