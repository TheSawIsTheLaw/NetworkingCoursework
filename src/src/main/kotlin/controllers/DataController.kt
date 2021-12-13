package controllers

import controllers.services.DataService
import domain.dtos.*
import domain.response.ResponseCreator
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

//@RequestMapping("/api/v1/data")
@RestController
class DataController(
    val dataService: DataService,
) {
    //    @GetMapping
    fun getData(token: String, username: String, measurementsNames: List<String>): ResponseEntity<*> {
        val outList: List<MeasurementDTO>
        try {
            outList =
                dataService.getMeasurements(
                    token, username,
                    measurementsNames
                )
        } catch (exc: Exception) {
            return ResponseCreator.internalServerErrorResponse(
                "Data server is dead :(",
                "Let's dance on its grave!"
            )
        }

        return ResponseEntity(
            ResponseMeasurementsDTO(outList),
            HttpStatus.OK
        )
    }

    //    @PostMapping
    fun addData(token: String, username: String, measurementsList: AcceptMeasurementsListDTO): ResponseEntity<*> {
        try {
            dataService.sendMeasurements(token, username, measurementsList)
        } catch (exc: Exception) {
            return ResponseCreator.internalServerErrorResponse(
                "Data server is dead :(",
                "Let's dance on its grave!"
            )
        }

        return ResponseCreator.okResponse(
            "Measurements were carefully sent",
            "We know all about you now >:c"
        )
    }
}