package uk.co.frankz.hmcts.dts.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.co.frankz.hmcts.dts.dto.TaskDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    TaskController testSubject;

    Mapper realMapper = new Mapper();

    @Mock
    TaskStore mockTaskStore;

    @Captor
    ArgumentCaptor<TaskWithId> saveCaptor;

    @BeforeEach
    void setup() {
        testSubject = new TaskController(mockTaskStore, realMapper);
    }

    @Test
    void shouldWelcome() {
        //when
        ResponseEntity<String> actual = testSubject.welcome();

        assertEquals(OK, actual.getStatusCode());
        assertEquals("Welcome to HM Courts and Tribunal Service Developer Technical Test", actual.getBody());
    }

    @Test
    void shouldStoreEntityFromDtoOnCreate() {

        // given
        TaskDto given = new TaskDto();
        String expectedTitle = "tit for tat";
        given.setTitle(expectedTitle);
        String expectedId = "123";
        TaskWithId mockStored = new TaskWithId();
        mockStored.setId(expectedId);
        when(mockTaskStore.save(any())).thenReturn(mockStored);

        // when
        ResponseEntity<TaskDto> actual = testSubject.createTask(given);

        // then
        assertEquals(OK, actual.getStatusCode());
        verify(mockTaskStore).save(saveCaptor.capture());
        assertEquals(expectedTitle, saveCaptor.getValue().getTitle());
        assertEquals(expectedId, actual.getBody().getId());
    }

}
