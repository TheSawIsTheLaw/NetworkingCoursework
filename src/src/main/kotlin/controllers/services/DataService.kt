package controllers.services

import data.CharRepositoryImpl
import domain.dtos.AcceptMeasurementsListDTO
import domain.dtos.MeasurementDTO
import domain.dtos.MeasurementData
import domain.dtos.MeasurementDataWithoutTime
import domain.logicentities.DSDataAccessInfo
import domain.logicentities.DSDataAddInfo
import domain.logicentities.DSMeasurement
import domain.logicentities.DSMeasurementList
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DataService(private val charRepository: CharRepositoryImpl)
{
    private fun getMeasurement(
        token: String,
        bucketName: String,
        charName: String
    ): List<DSMeasurement>
    {
        val gotInformation = charRepository.get(
            DSDataAccessInfo(
                token,
                bucketName,
                Pair(0, 0),
                charName
            )
        )

        return gotInformation.map { DSMeasurement(charName, it.value, it.time) }
    }

    fun getMeasurements(
        token: String,
        bucketName: String,
        requiredNames: List<String>
    ): List<MeasurementDTO>
    {
        val outMeasurements: MutableList<MeasurementDTO> =
            mutableListOf()

        for (charName in requiredNames)
        {
            outMeasurements.add(
                MeasurementDTO(
                    charName, getMeasurement(token, bucketName, charName).map {
                        MeasurementData(
                            it.value, it.time
                        )
                    }
                )
            )
        }

        return outMeasurements
    }

    private fun sendMeasurement(
        token: String,
        bucketName: String,
        charName: String,
        chars: List<MeasurementDataWithoutTime>
    )
    {
        charRepository.add(
            DSDataAddInfo(
                token,
                bucketName, DSMeasurementList
                    (
                    charName,
                    chars.map {
                        DSMeasurement(
                            charName,
                            it.value,
                            Instant.EPOCH
                        )
                    })
            )
        )
    }

    fun sendMeasurements(
        token: String,
        bucketName: String,
        chars: AcceptMeasurementsListDTO
    )
    {
        for (measurement in chars.measurements)
        {
            sendMeasurement(
                token, bucketName,
                measurement.measurement, measurement.values
            )
        }
    }
}