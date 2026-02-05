package uk.co.frankz.hmcts.dts.aws.infra;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.DomainName;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.certificatemanager.ICertificate;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;

import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.apiBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.apiDomainBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.createRoute;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.createTaskBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.deleteRoute;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.deleteTaskBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.dnsEntryBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.getAllRoute;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.getRoute;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.retrieveTaskBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.rootRoute;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.rootTaskBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.tableBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.updateRoute;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.updateStatusRoute;
import static uk.co.frankz.hmcts.dts.aws.infra.BackEndComponent.updateTaskBuilder;
import static uk.co.frankz.hmcts.dts.aws.infra.MyEnvironment.awsDefaultProfile;
import static uk.co.frankz.hmcts.dts.aws.infra.ProvisionedComponent.SUBDOMAIN_NAME;
import static uk.co.frankz.hmcts.dts.aws.infra.ProvisionedComponent.SUBDOMAIN_PREFIX;
import static uk.co.frankz.hmcts.dts.aws.infra.ProvisionedComponent.certFinder;

public class BackEndStack extends Stack {

    public BackEndStack(Construct scope, String id) {
        this(scope, id, awsDefaultProfile());
    }

    public BackEndStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        tableBuilder.build(this, "MyTable");

        Function defaultLambda = rootTaskBuilder.build(this, "RootLambda");
        Function createLambda = createTaskBuilder.build(this, "CreateLambda");
        Function deleteLambda = deleteTaskBuilder.build(this, "DeleteLambda");
        Function retrieveLambda = retrieveTaskBuilder.build(this, "RetrieveLambda");
        Function updateLambda = updateTaskBuilder.build(this, "UpdateLambda");

        AddRoutesOptions root = rootRoute.build(defaultLambda, "ApiGatewayRouteToLambda1");
        List<AddRoutesOptions> routeOut = Arrays.asList(
            createRoute.build(createLambda, "ApiGatewayRouteToLambda2"),
            deleteRoute.build(deleteLambda, "ApiGatewayRouteToLambda3"),
            getRoute.build(retrieveLambda, "ApiGatewayRouteToLambda4"),
            getAllRoute.build(retrieveLambda, "ApiGatewayRouteToLambda5"),
            updateRoute.build(updateLambda, "ApiGatewayRouteToLambda6"),
            updateStatusRoute.build(updateLambda, "ApiGatewayRouteToLambda7")
        );

        ICertificate cert = certFinder.find(this);
        DomainName subDomain = apiDomainBuilder.build(this, "MySubDomain", SUBDOMAIN_NAME, cert);

        HttpApi api = apiBuilder.build(this, "MyApi", subDomain, root.getIntegration());
        apiBuilder.buildMap(this, "MyBasePathMap", api, subDomain, "task");

        // add all the lambda routes outgoing from apigateway
        routeOut.forEach(api::addRoutes);

        // add the incoming route to apigateway
        dnsEntryBuilder.build(this, "MyAliasDNS", SUBDOMAIN_PREFIX, subDomain);
    }
}
