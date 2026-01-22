package uk.co.frankz.hmcts.dts.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService extends uk.co.frankz.hmcts.dts.service.TaskService<TaskWithId> {

    @Autowired
    public TaskService(TaskStore taskStore) {

        super(taskStore);
    }
}
