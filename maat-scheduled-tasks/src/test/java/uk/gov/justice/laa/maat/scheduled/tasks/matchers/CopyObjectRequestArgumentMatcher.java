package uk.gov.justice.laa.maat.scheduled.tasks.matchers;

import org.mockito.ArgumentMatcher;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;

public class CopyObjectRequestArgumentMatcher implements ArgumentMatcher<CopyObjectRequest> {

    private final String sourceKey;
    private final String destinationKey;

    public CopyObjectRequestArgumentMatcher(final String sourceKey, final String destinationKey) {
        this.sourceKey = sourceKey;
        this.destinationKey = destinationKey;
    }

    @Override
    public boolean matches(CopyObjectRequest copyObjectRequest) {
        if (copyObjectRequest == null) {
            return false;
        }

        return sourceKey.equals(copyObjectRequest.sourceKey())
            && destinationKey.equals(copyObjectRequest.destinationKey());
    }
}
