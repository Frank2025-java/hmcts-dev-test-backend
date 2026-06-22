package uk.co.frankz.hmcts.dts.aws.dynamodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClientExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DescribeTableEnhancedResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Wrapper class around DynamoDb table with basic functions.
 * It hides the static initialiser for DynomoDb Access, which is static for performance reasons,
 * and which is also supposed to be thread safe.
 *
 * <p>This class instance can be injected into data access functionality classes, which then can
 * have unit tests without dynamoDB access. Because of With that static initialisation, there is
 * no unit test for this class.
 *
 * @param <T> where T is a entity class that should have annotation @DynamoDbBean.
 *            See {@link IdemPotencyStoreImpl} and {@link TaskStoreImpl}
 */
public class DynamoDbTableProvider<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DynamoDbTableProvider.class);

    private static final DynamoDbClient dynamoAccess = DynamoDbClient.builder().region(Region.EU_WEST_1).build();

    private final DynamoDbTable<T> table;

    public DynamoDbTableProvider(String tableName, Class<T> dynamoDbBean) {
        this(tableName, dynamoDbBean, null);
    }

    public DynamoDbTableProvider(String tableName, Class<T> dynamoDbBean, DynamoDbEnhancedClientExtension extension) {

        DynamoDbEnhancedClient.Builder enhancedBuilder = DynamoDbEnhancedClient
            .builder()
            .dynamoDbClient(dynamoAccess);

        if (extension != null) {
            enhancedBuilder = enhancedBuilder.extensions(extension);
        }

        this.table = enhancedBuilder.build().table(tableName, TableSchema.fromBean(dynamoDbBean));
    }

    public T find(String id) {
        Key findKey = Key.builder().partitionValue(id).build();

        try {
            return table.getItem(findKey);
        } catch (Exception e) {
            LOG.warn(e.toString());
            return null;
        }
    }

    public void save(T entity) {

        try {
            table.putItem(entity);
        } catch (Exception e) {
            LOG.error("Save failed: ", e);
            throw e;
        }
    }

    public void update(T entity) {

        try {
            table.updateItem(entity);
        } catch (Exception e) {
            LOG.error("Update failed: ", e);
            throw e;
        }
    }

    public void delete(String id) {

        T found = find(id);
        if (found != null) {
            try {
                table.deleteItem(found);
            } catch (Exception e) {
                LOG.warn("Silent delete - " + e);
            }
        }
    }

    public Iterable<T> findAll() {

        try {
            return table.scan().items().stream().toList();
        } catch (Exception e) {
            LOG.error("Scan failed: ", e);
            throw e;
        }
    }

    public DescribeTableEnhancedResponse describe() {
        return table.describeTable();
    }
}
