package com.kailasnath.locationtracker.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.kailasnath.locationtracker.Model.BusLocation;
import com.kailasnath.locationtracker.Model.BusLocationAndRecordStatus;
import com.kailasnath.locationtracker.service.BusLocationService;
import com.kailasnath.locationtracker.service.LocationCoordService;

@Controller
public class BusLocationController {

    @Autowired
    BusLocationService busLocationService;

    @Autowired
    LocationCoordService locationCoordService;

    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        SseEmitter sseEmitter = new SseEmitter();

        emitters.add(sseEmitter);

        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        sseEmitter.onTimeout(() -> emitters.remove(sseEmitter));
        return sseEmitter;
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateBusLocation(
            @NonNull @RequestBody BusLocationAndRecordStatus busLocationAndRecordStatus) {

        BusLocation busLocation = new BusLocation(
                busLocationAndRecordStatus.getBus_id(),
                busLocationAndRecordStatus.getLatitude(),
                busLocationAndRecordStatus.getLongitude());

        for (SseEmitter sseEmitter : emitters) {
            try {
                sseEmitter.send(SseEmitter.event().name("location-updated").data(busLocation));
            } catch (IOException e) {
                sseEmitter.complete();
                e.printStackTrace();
            }
        }

        busLocationService.updateLocation(busLocation);

        if (busLocationAndRecordStatus.isRecord())
            locationCoordService.addLocationCoord(busLocation);

        return ResponseEntity.ok("Location Updated");
    }

    @GetMapping("find")
    public String getLocationView(@RequestParam("id") int id, Model model) {

        BusLocation busLocation = busLocationService.getLocation(id);
        model.addAttribute("buslocation", busLocation);

        double[][] route = locationCoordService.getRouteCoords(id);
        model.addAttribute("route", route);

        return "viewLocation";
    }

    @GetMapping("/find-bus/{id}")
    @ResponseBody
    public BusLocation getLocation(@PathVariable("id") int id, Model model) {

        BusLocation busLocation = busLocationService.getLocation(id);
        return busLocation;
    }
}