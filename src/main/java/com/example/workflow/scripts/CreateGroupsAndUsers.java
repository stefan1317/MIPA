package com.example.workflow.scripts;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateGroupsAndUsers implements CommandLineRunner {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void run(String... args) throws Exception {
        authorizationService.createAuthorizationQuery()
                .list()
                .stream().filter(auth -> "clienti".equals(auth.getGroupId()) || "leasing".equals(auth.getGroupId()) )
                .forEach(auth -> authorizationService.deleteAuthorization(auth.getId()));

        String[] groups = {"clienti", "leasing"};

        for (String group : groups) {
            Authorization auth = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            auth.setGroupId(group);
            auth.setResource(org.camunda.bpm.engine.authorization.Resources.APPLICATION);
            auth.setResourceId("*");
            auth.addPermission(org.camunda.bpm.engine.authorization.Permissions.ACCESS);
            authorizationService.saveAuthorization(auth);

            Authorization proc = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            proc.setGroupId(group);
            proc.setResource(Resources.PROCESS_DEFINITION);
            auth.setResourceId("*");
            proc.addPermission(Permissions.READ);
            proc.addPermission(Permissions.CREATE_INSTANCE);
            authorizationService.saveAuthorization(proc);

            Authorization task = authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
            task.setGroupId(group);
            task.setResource(Resources.TASK);
            task.setResourceId("*");
            task.addPermission(Permissions.READ);
            task.addPermission(Permissions.UPDATE);
            task.addPermission(Permissions.READ_HISTORY);
            authorizationService.saveAuthorization(task);
        }

        if (identityService.createGroupQuery().groupId("clienti").singleResult() == null) {
            Group clienti = identityService.newGroup("clienti");
            clienti.setName("Clienti");
            clienti.setType("Client");
            identityService.saveGroup(clienti);
        }

        if (identityService.createGroupQuery().groupId("leasing").singleResult() == null) {
            Group leasing = identityService.newGroup("leasing");
            leasing.setName("Leasing");
            leasing.setType("Companie Leasing");
            identityService.saveGroup(leasing);
        }

        if (identityService.createUserQuery().userId("costin").singleResult() == null) {
            User user = identityService.newUser("costin");
            user.setPassword("parola");
            user.setFirstName("Costin");
            identityService.saveUser(user);
        }

        if (identityService.createUserQuery().userId("raul").singleResult() == null) {
            User user = identityService.newUser("raul");
            user.setPassword("parola");
            user.setFirstName("Raul");
            identityService.saveUser(user);
        }

        List<Group> groupCostin = identityService
                .createGroupQuery()
                .groupMember("costin")
                .list();

        boolean isCostin = groupCostin.stream()
                .anyMatch(g -> g.getId().equals("clienti"));

        if (!isCostin) {
            identityService.createMembership("costin", "clienti");
        }

        List<Group> groupRaul = identityService
                .createGroupQuery()
                .groupMember("raul")
                .list();

        boolean isRaul = groupRaul.stream()
                .anyMatch(g -> g.getId().equals("leasing"));

        if (!isRaul) {
            identityService.createMembership("raul", "leasing");
        }
    }
}
