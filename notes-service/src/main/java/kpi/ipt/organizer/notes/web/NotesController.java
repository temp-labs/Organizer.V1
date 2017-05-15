package kpi.ipt.organizer.notes.web;

import kpi.ipt.organizer.auth.AuthUtils;
import kpi.ipt.organizer.notes.exceptions.NoteNotFoundException;
import kpi.ipt.organizer.notes.model.Note;
import kpi.ipt.organizer.notes.model.NoteProperties;
import kpi.ipt.organizer.notes.model.request.SearchRequest;
import kpi.ipt.organizer.notes.service.NotesService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
public class NotesController {

    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Note> getUserNotes() {
        long userId = AuthUtils.currentUserId();

        return notesService.getUserNotes(userId);
    }

    @RequestMapping(path = "/search", method = RequestMethod.POST)
    public List<Note> searchNotes(@RequestBody SearchRequest request) {
        long userId = AuthUtils.currentUserId();

        return notesService.searchNotes(userId, request.getQuery());
    }

    @RequestMapping(path = "/{noteId}", method = RequestMethod.GET)
    public Note getNote(@PathVariable("noteId") String noteId) {
        long userId = AuthUtils.currentUserId();

        Note note = notesService.getNote(userId, noteId);
        if (note == null) {
            throw new NoteNotFoundException();
        }

        return note;
    }

    @RequestMapping(method = RequestMethod.POST)
    public Map<String, String> createNote(@RequestBody NoteProperties noteProperties) {
        long userId = AuthUtils.currentUserId();
        String createdNoteId = notesService.createNote(userId, noteProperties);

        return Collections.singletonMap("noteId", createdNoteId);
    }

    @RequestMapping(path = "/{noteId}", method = RequestMethod.PUT)
    public SuccessResponse editNote(@PathVariable("noteId") String noteId, @RequestBody NoteProperties noteProperties) {
        long userId = AuthUtils.currentUserId();

        if (!notesService.updateNote(userId, noteId, noteProperties)) {
            throw new NoteNotFoundException();
        }

        return SuccessResponse.INSTANCE;
    }

    @RequestMapping(path = "/{noteId}", method = RequestMethod.DELETE)
    public SuccessResponse deleteNote(@PathVariable("noteId") String noteId) {
        long userId = AuthUtils.currentUserId();

        if (!notesService.deleteNote(userId, noteId)) {
            throw new NoteNotFoundException();
        }

        return SuccessResponse.INSTANCE;
    }

    @Getter
    private static class SuccessResponse {

        private static final SuccessResponse INSTANCE = new SuccessResponse();

        private final boolean success = true;
    }

    /* ************************ Testing methods ************************ */

    @Autowired
    private NotesMongoRepository notesRepository;

    @RequestMapping(path = "/test/getAll", method = RequestMethod.GET)
    public List<Note> getAllNotes() {
        return notesRepository.findAll();
    }

    @RequestMapping(path = "/test/{noteId}", method = RequestMethod.DELETE)
    public SuccessResponse deleteNoteById(@PathVariable("noteId") String noteId) {
        notesRepository.delete(noteId);
        return SuccessResponse.INSTANCE;
    }
}
