package uk.gov.justice.laa.maat.scheduled.tasks.matchers;

import org.mockito.ArgumentMatcher;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

public class DeleteObjectRequestArgumentMatcher implements ArgumentMatcher<DeleteObjectRequest> {

    private final String objectKey;

    public DeleteObjectRequestArgumentMatcher(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public boolean matches(DeleteObjectRequest deleteObjectRequest) {
        return objectKey.equals(deleteObjectRequest.key());
    }
}
