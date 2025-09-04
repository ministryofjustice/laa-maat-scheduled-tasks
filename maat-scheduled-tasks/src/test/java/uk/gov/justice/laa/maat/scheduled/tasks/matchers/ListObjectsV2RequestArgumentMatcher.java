package uk.gov.justice.laa.maat.scheduled.tasks.matchers;

import org.mockito.ArgumentMatcher;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

public class ListObjectsV2RequestArgumentMatcher implements ArgumentMatcher<ListObjectsV2Request> {
    private final String continuationToken;

    public ListObjectsV2RequestArgumentMatcher(String continuationToken) {
        this.continuationToken = continuationToken;
    }

    @Override
    public boolean matches(ListObjectsV2Request listObjectsV2Request) {
        return (continuationToken == null && listObjectsV2Request.continuationToken() == null)
            || continuationToken.equals(listObjectsV2Request.continuationToken());
    }
}
