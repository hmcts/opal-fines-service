package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interface_messages")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interface_message_id_seq")
    @SequenceGenerator(name = "interface_message_id_seq", sequenceName = "interface_message_id_seq", allocationSize = 1)
    @Column(name = "interface_message_id")
    private Long interfaceMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_job_id", nullable = false)
    private InterfaceJobEntity interfaceJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_file_id")
    private InterfaceFileEntity interfaceFile;

    @Column(name = "message_type", nullable = false, length = 10)
    private String messageType;

    @Column(name = "message_text", nullable = false, length = 500)
    private String messageText;

    @Column(name = "record_index")
    private Long recordIndex;

    @Column(name = "record_detail")
    private String recordDetail;
}
