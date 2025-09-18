package uk.gov.justice.laa.maat.scheduled.tasks.xhibit.dto;

import java.util.List;
import java.util.stream.Stream;

/**
 * Represents one page of record sheets retrieved from S3.
 */
public record RecordSheetsPage(
        List<RecordSheet> retrieved,
        List<RecordSheet> errored,
        String continuationToken,
        boolean complete
) {

    public RecordSheetsPage {
        retrieved = List.copyOf(retrieved);
        errored = List.copyOf(errored);
    }

    public static RecordSheetsPage empty() {
        return new RecordSheetsPage(List.of(), List.of(), null, false);
    }

    public RecordSheetsPage withRetrieved(List<RecordSheet> moreRetrieved) {
        return new RecordSheetsPage(
                concat(this.retrieved, moreRetrieved),
                this.errored,
                this.continuationToken,
                this.complete
        );
    }

    public RecordSheetsPage withErrored(List<RecordSheet> moreErrored) {
        return new RecordSheetsPage(
                this.retrieved,
                concat(this.errored, moreErrored),
                this.continuationToken,
                this.complete
        );
    }

    public RecordSheetsPage next(String continuationToken, boolean complete) {
        return new RecordSheetsPage(this.retrieved, this.errored, continuationToken, complete);
    }

    public static RecordSheetsPage complete(List<RecordSheet> retrieved,
            List<RecordSheet> errored) {
        return new RecordSheetsPage(retrieved, errored, null, true);
    }

    private static <T> List<T> concat(List<T> a, List<T> b) {
        return Stream.concat(a.stream(), b.stream()).toList();
    }
}

