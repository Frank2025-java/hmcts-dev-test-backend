package uk.co.frankz.hmcts.dts.aws.infra;

import uk.co.frankz.hmcts.dts.service.Action;

import static software.amazon.awscdk.services.apigatewayv2.HttpMethod.DELETE;
import static software.amazon.awscdk.services.apigatewayv2.HttpMethod.GET;
import static software.amazon.awscdk.services.apigatewayv2.HttpMethod.POST;
import static software.amazon.awscdk.services.apigatewayv2.HttpMethod.PUT;
import static uk.co.frankz.hmcts.dts.aws.infra.ProvisionedComponent.DOMAIN;

public interface BackEndComponent {

    LambdaBuilder createTaskBuilder = new LambdaBuilder(
        "CreateTask",
        "functions.jar",
        "uk.co.frankz.hmcts.dts.aws.lambda.CreateTaskHandler"
    );

    LambdaBuilder deleteTaskBuilder = new LambdaBuilder(
        "DeleteTask",
        "functions.jar",
        "uk.co.frankz.hmcts.dts.aws.lambda.DeleteTaskHandler"
    );

    LambdaBuilder retrieveTaskBuilder = new LambdaBuilder(
        "RetrieveTask",
        "functions.jar",
        "uk.co.frankz.hmcts.dts.aws.lambda.RetrieveTaskHandler"
    );

    LambdaBuilder updateTaskBuilder = new LambdaBuilder(
        "UpdateTask",
        "functions.jar",
        "uk.co.frankz.hmcts.dts.aws.lambda.UpdateTaskHandler"
    );

    LambdaBuilder rootTaskBuilder = new LambdaBuilder(
        "RootTask",
        "functions.jar",
        "uk.co.frankz.hmcts.dts.aws.lambda.RootTaskHandler"
    );

    LambdaRouteBuilder createRoute = new LambdaRouteBuilder(Action.PATH.CREATE, POST);
    LambdaRouteBuilder deleteRoute = new LambdaRouteBuilder(Action.PATH.DELETE, DELETE);
    LambdaRouteBuilder getRoute = new LambdaRouteBuilder(Action.PATH.GET, GET);
    LambdaRouteBuilder getAllRoute = new LambdaRouteBuilder(Action.PATH.GET_ALL, GET);
    LambdaRouteBuilder updateRoute = new LambdaRouteBuilder(Action.PATH.UPDATE, POST);
    LambdaRouteBuilder updateStatusRoute = new LambdaRouteBuilder(Action.PATH.UPDATE_STATUS, PUT);
    LambdaRouteBuilder rootRoute = new LambdaRouteBuilder(Action.PATH.ROOT, GET);

    ApiGatewayBuilder apiBuilder = new ApiGatewayBuilder("LambdaApi");

    SubDomainBuilder apiDomainBuilder = new SubDomainBuilder();
    DnsEntryBuilder dnsEntryBuilder = new DnsEntryBuilder(DOMAIN);
}
