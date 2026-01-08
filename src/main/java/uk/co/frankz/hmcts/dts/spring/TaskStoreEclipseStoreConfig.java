package uk.co.frankz.hmcts.dts.spring;

import org.eclipse.serializer.reflect.ClassLoaderProvider;
import org.eclipse.store.integrations.spring.boot.types.configuration.EclipseStoreProperties;
import org.eclipse.store.integrations.spring.boot.types.factories.EmbeddedStorageFoundationFactory;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageFoundation;
import org.eclipse.store.storage.types.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import software.xdev.spring.data.eclipse.store.repository.config.EclipseStoreClientConfiguration;
import software.xdev.spring.data.eclipse.store.repository.config.EnableEclipseStoreRepositories;

import java.nio.file.Path;

@Configuration
@EnableEclipseStoreRepositories
public class TaskStoreEclipseStoreConfig extends EclipseStoreClientConfiguration {

    private final EclipseStoreProperties properties;
    private final EmbeddedStorageFoundationFactory foundationFactory;

    @Autowired
    public TaskStoreEclipseStoreConfig(
        @Autowired EclipseStoreProperties defaultEclipseStoreProperties,
        @Autowired EmbeddedStorageFoundationFactory defaultEclipseStoreProvider,
        @Autowired ClassLoaderProvider classLoaderProvider) {

        super(defaultEclipseStoreProperties, defaultEclipseStoreProvider, classLoaderProvider);

        properties = defaultEclipseStoreProperties;
        foundationFactory = defaultEclipseStoreProvider;
    }

    @Override
    public EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation() {
        return EmbeddedStorage.Foundation(Storage.Configuration(Storage.FileProvider(Path.of("eclipse-storage-task"))));

    }

}
