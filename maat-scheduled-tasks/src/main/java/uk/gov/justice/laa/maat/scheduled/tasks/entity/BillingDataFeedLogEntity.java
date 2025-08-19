package uk.gov.justice.laa.maat.scheduled.tasks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "billing_data_feed_log", schema = "TOGDATA")
public class BillingDataFeedLogEntity {
    
    @Id
    @SequenceGenerator(name = "billing_log_gen_seq", sequenceName = "S_GENERAL_SEQUENCE", allocationSize = 1, schema = "TOGDATA")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billing_log_gen_seq")
    @Column(name = "id")
    private Integer id;
    @Column(name = "record_type")
    private String recordType;
    @Column(name = "date_created")
    private LocalDateTime dateCreated;
    @Column(name = "payload")
    private String payload;
}
