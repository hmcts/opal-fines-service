package uk.gov.hmcts.opal.entity;

import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.DB_PROC_NAME;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.INTERFACE_FILE_ID;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.INTERFACE_JOB_ID;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.JPA_PROC_NAME;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.MESSAGE_DATA;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.MESSAGE_TEXT;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.MESSAGE_TYPE;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.RECORD_DETAIL;
import static uk.gov.hmcts.opal.entity.interfacemessage.InterfaceMessageStoredProcedureNames.RECORD_INDEX;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "interface_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedStoredProcedureQuery(name = JPA_PROC_NAME, procedureName = DB_PROC_NAME, parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = INTERFACE_JOB_ID, type = Long.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = MESSAGE_TYPE, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = MESSAGE_TEXT, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = INTERFACE_FILE_ID, type = Long.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = RECORD_INDEX, type = Long.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = RECORD_DETAIL, type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.IN, name = MESSAGE_DATA, type = String.class)
})
public class InterfaceMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interface_message_id_seq_generator")
    @SequenceGenerator(name = "interface_message_id_seq_generator",
        sequenceName = "interface_message_id_seq", allocationSize = 1)
    @Column(name = "interface_message_id", nullable = false)
    private Long interfaceMessageId;

    @Column(name = "interface_job_id", nullable = false)
    private Long interfaceJobId;

    @Column(name = "interface_file_id")
    private Long interfaceFileId;

    @Column(name = "message_type", length = 10, nullable = false)
    private String messageType;

    @Column(name = "message_text", length = 500, nullable = false)
    private String messageText;

    @Column(name = "record_index")
    private Long recordIndex;

    @Column(name = "record_detail")
    private String recordDetail;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message_data", columnDefinition = "json")
    private String messageData;
}
