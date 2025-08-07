package uk.gov.justice.laa.maat.scheduled.tasks.matchers;

import org.mockito.ArgumentMatcher;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public class GetObjectRequestArgumentMatcher implements ArgumentMatcher<GetObjectRequest> {
    private final String objectKey;

    public GetObjectRequestArgumentMatcher(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public boolean matches(GetObjectRequest getObjectRequest) {
        return objectKey.equals(getObjectRequest.key());
    }
}
