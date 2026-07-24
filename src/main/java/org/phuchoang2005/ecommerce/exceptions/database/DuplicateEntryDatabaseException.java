package org.phuchoang2005.ecommerce.exceptions.database;


import org.phuchoang2005.ecommerce.enums.HttpStatusEnum;
import org.phuchoang2005.ecommerce.exceptions.BaseException;

// Extends BaseException directly (not DatabaseException, whose constructor
// hardcodes a 500) so getStatusCode()/getError() carry the 409 Duplicate status
// that GlobalExceptionFilter.handleException() actually reads.
public class DuplicateEntryDatabaseException extends BaseException {
    public DuplicateEntryDatabaseException(String message){
        super(
                HttpStatusEnum.DUPLICATE_ENTRY.code(),
                HttpStatusEnum.DUPLICATE_ENTRY.message(),
                message
        );
    }
}
