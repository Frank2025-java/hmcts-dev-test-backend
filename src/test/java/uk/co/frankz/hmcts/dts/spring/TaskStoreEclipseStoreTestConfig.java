package uk.co.frankz.hmcts.dts.spring;

import org.eclipse.serializer.reflect.ClassLoaderProvider;
import org.eclipse.store.integrations.spring.boot.types.configuration.EclipseStoreProperties;
import org.eclipse.store.integrations.spring.boot.types.factories.EmbeddedStorageFoundationFactory;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageFoundation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import software.xdev.spring.data.eclipse.store.repository.config.EclipseStoreClientConfiguration;
import software.xdev.spring.data.eclipse.store.repository.config.EnableEclipseStoreRepositories;

@TestConfiguration
@EnableEclipseStoreRepositories
public class TaskStoreEclipseStoreTestConfig extends EclipseStoreClientConfiguration {

    private final EclipseStoreProperties properties;
    private final EmbeddedStorageFoundationFactory foundationFactory;

    @Autowired
    public TaskStoreEclipseStoreTestConfig(
        @Autowired EclipseStoreProperties defaultEclipseStoreProperties,
        @Autowired EmbeddedStorageFoundationFactory defaultEclipseStoreProvider,
        @Autowired ClassLoaderProvider classLoaderProvider) {

        super(defaultEclipseStoreProperties, defaultEclipseStoreProvider, classLoaderProvider);

        properties = defaultEclipseStoreProperties;
        foundationFactory = defaultEclipseStoreProvider;
    }

//    @Bean
//    @Primary
//    EmbeddedStorageManager inMemoryStorageManager(
//        @Autowired EclipseStoreProperties myConfiguration,
//        @Autowired EmbeddedStorageManagerFactory managerFactory,
//        @Autowired EmbeddedStorageFoundationFactory foundationFactory) {
//
//        // default is in-memory storage
//        //myConfiguration.setStorageDirectory(tempDir.toFile().getPath());
//
//        EmbeddedStorageFoundation<?> storageFoundation = foundationFactory.createStorageFoundation(myConfiguration);
//
//        return managerFactory.createStorage(storageFoundation, false);
//    }

    @Override
    public EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation() {
        //return EmbeddedStorage.Foundation(Storage.Configuration(Storage.FileProvider(Path.of("unittest-storage"))));

        // default properties is in-memory storage
        return foundationFactory.createStorageFoundation(properties);
    }

}
