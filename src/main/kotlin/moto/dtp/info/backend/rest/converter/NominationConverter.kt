package moto.dtp.info.backend.rest.converter

import moto.dtp.info.backend.datasources.NominationClient
import org.springframework.stereotype.Service

@Service
class NominationConverter {
    fun toNominationResponse(result: NominationClient.NominationResult): String {
        with(result) {
            return listOfNotNull(
                if (address[COUNTRY_CODE] != "ru") address[COUNTRY_CODE] else null,
                address[CITY],
                address[ROAD],
                address[HOUSE_NUMBER]
            ).joinToString(", ")
        }
    }

    companion object {
        private const val HOUSE_NUMBER = "house_number" //: "8А",
        private const val ROAD = "road" //: "Люблинская улица",
        private const val SUBURB = "suburb" //: "район Текстильщики",
        private const val CITY = "city" //: "Москва",
        private const val STATE = "state" //: "Москва",
        private const val REGION = "region" //: "Центральный федеральный округ",
        private const val POSTCODE = "postcode" //: "109390",
        private const val COUNTRY = "country" //: "Россия",
        private const val COUNTRY_CODE = "country_code" //: "ru"
    }
}