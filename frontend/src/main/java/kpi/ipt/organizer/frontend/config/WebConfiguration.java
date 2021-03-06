package kpi.ipt.organizer.frontend.config;

import kpi.ipt.organizer.frontend.converters.EventToEventUiModelConverter;
import kpi.ipt.organizer.frontend.converters.NoteToNoteUiModelConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new EventToEventUiModelConverter());
        registry.addConverter(new NoteToNoteUiModelConverter());
    }
}
