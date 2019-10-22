package io.agileintelligence.ppmtool.services;

import io.agileintelligence.ppmtool.domain.Backlog;
import io.agileintelligence.ppmtool.domain.Project;
import io.agileintelligence.ppmtool.domain.ProjectTask;
import io.agileintelligence.ppmtool.exceptions.ProjectNotFoundException;
import io.agileintelligence.ppmtool.repositories.BacklogRepository;
import io.agileintelligence.ppmtool.repositories.ProjectRepository;
import io.agileintelligence.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTaskService {
    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username) {
        // PTs added to a specific project, project not null, BL exists
        Backlog backlog =  projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog();
        // set the BL to PT
        projectTask.setBacklog(backlog);
        // Project sequence should follow increment by 1 for each new one created ID-1, ID-2, etc..
        Integer backlogSequence = backlog.getPTSequence();
        backlogSequence++;
        backlog.setPTSequence(backlogSequence);
        // Add sequence to Project Task
        projectTask.setProjectSequence(backlog.getProjectIdentifier() + "-"+ backlogSequence);
        projectTask.setProjectIdentifier(projectIdentifier);

        // Initial status when null
        if(projectTask.getStatus() == "" || projectTask.getStatus() == null) {
            projectTask.setStatus(("TODO"));
        }

        // Initial priority when null
        if(projectTask.getPriority() == null || projectTask.getPriority() == 0) {
            projectTask.setPriority(3);
        }

        return projectTaskRepository.save(projectTask);
    }

    public Iterable<ProjectTask> findBacklogById(String id, String username) {
        projectService.findProjectByIdentifier(id, username);

        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username) {
        // make sure we are searching on an existing backlog
        projectService.findProjectByIdentifier(backlog_id, username);

        // make sure task exists
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
        if(projectTask == null) {
            throw new ProjectNotFoundException("Project Task " + pt_id + " not found");
        }
        // make sure that back/project in path corresponds to the right project
        if(!projectTask.getProjectIdentifier().equals(backlog_id)) {
            throw new ProjectNotFoundException("Project Task " + pt_id + " does not exist in project " + backlog_id);
        }

        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        projectTask = updatedTask;

        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        projectTaskRepository.delete(projectTask);
    }
}
