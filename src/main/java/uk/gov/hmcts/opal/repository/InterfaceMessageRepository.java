package uk.gov.hmcts.opal.repository;

import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.INTERFACE_FILE_ID;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.INTERFACE_JOB_ID;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.MESSAGE_DATA;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.MESSAGE_TEXT;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.MESSAGE_TYPE;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.RECORD_DETAIL;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.RECORD_INDEX;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.InterfaceMessageEntity;

@Repository
public interface InterfaceMessageRepository extends JpaRepository<InterfaceMessageEntity, Long> {

    @Modifying
    @Query(value = """
        CALL p_insert_interface_message(
            :pi_interface_job_id,
            :pi_message_type,
            :pi_message_text,
            :pi_interface_file_id,
            :pi_record_index,
            :pi_record_detail,
            CAST(:pi_message_data AS json)
        )
        """, nativeQuery = true)
    void insertInterfaceMessage(@Param(INTERFACE_JOB_ID) Long interfaceJobId,
                                @Param(MESSAGE_TYPE) String messageType,
                                @Param(MESSAGE_TEXT) String messageText,
                                @Param(INTERFACE_FILE_ID) Long interfaceFileId,
                                @Param(RECORD_INDEX) Long recordIndex,
                                @Param(RECORD_DETAIL) String recordDetail,
                                @Param(MESSAGE_DATA) String messageData);
}
