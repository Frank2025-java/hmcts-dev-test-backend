package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

/**
 * Utility class for generating a Table construct for CDK.
 */
public class TableBuilder {

    private final String name;

    public TableBuilder(String tableName) {
        this.name = tableName;
    }

    /**
     * Generates a Table construct.
     *
     * @param scope of construct
     * @param id    as CDK identifier in the generated asset files
     */
    public Table build(Construct scope, String id) {
        Attribute keyColumn = Attribute.builder()
            .name("id")
            .type(AttributeType.STRING)
            .build();

        return Table.Builder
            .create(scope, id)
            .tableName(this.name)
            .partitionKey(keyColumn)
            .billingMode(BillingMode.PAY_PER_REQUEST) // id think the usage is not predictable
            .removalPolicy(RemovalPolicy.DESTROY) // Use RETAIN for production
            .build();
    }
}
