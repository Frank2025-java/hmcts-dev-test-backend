package uk.co.frankz.hmcts.dts.aws;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskStoreImpl;
import uk.co.frankz.hmcts.dts.aws.dynamodb.TaskWithId;

public class TaskService extends uk.co.frankz.hmcts.dts.service.TaskService<TaskWithId> {

    public TaskService(AwsCredentialsProvider credentials) {

        super(new TaskStoreImpl(credentials));
    }
}
