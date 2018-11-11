package com.canehealth.controller;


import org.openmhealth.schema.domain.omh.DataPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class DefaultController extends WebMvcConfigurerAdapter {


//    @RequestMapping(value = {"/editor"}, method = RequestMethod.GET)
//    public ModelAndView editor() {
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("index");
//        return modelAndView;
//    }

//    @PostMapping("/students/{studentId}/courses")
//    public ResponseEntity<Void> registerStudentForCourse(
//            @PathVariable String studentId, @RequestBody Course newCourse) {
//
//        Course course = studentService.addCourse(studentId, newCourse);
//
//        if (course == null)
//            return ResponseEntity.noContent().build();
//
//        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
//                "/{id}").buildAndExpand(course.getId()).toUri();
//
//        return ResponseEntity.created(location).build();
//    }

    /**
     * Writes a data point.
     *
     * @param dataPoint the data point to write
     */
    // only allow clients with write scope to write data points
    // @PreAuthorize("#oauth2.clientHasRole('" + CLIENT_ROLE + "') and #oauth2.hasScope('" + DATA_POINT_WRITE_SCOPE + "')")
    @RequestMapping(value = "/datapoint", method = POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> writeDataPoint(@RequestBody @Valid DataPoint dataPoint) {

        return new ResponseEntity<>(CREATED);
    }

    @RequestMapping("/cog/token")
    public ModelAndView oauth2Token() {
        // forwards from /omh/token to /auth
        return new ModelAndView("forward:/auth");
    }

    @RequestMapping("/cog")
    public ModelAndView cog2console() {
        // forwards from /omh/token to /auth
        return new ModelAndView("forward:/console");
    }

//    @RequestMapping("/omh/auth")
//    public ModelAndView oauth() {
//        return new ModelAndView("forward:/redirectedUrl");
//    }

    @RequestMapping("/cog/dsu")
    public ModelAndView dsu() {
        return new ModelAndView("forward:/dsu");
    }

    @RequestMapping("/cog/shimmer")
    public ModelAndView shimmer() {
        // Authorize access from the console
        // http://<<shimmer-host>>:8083/data/{shimKey}/{endpoint}?username={userId}&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd&normalize={true|false}
        return new ModelAndView("forward:/shimmer/data");
    }
}
