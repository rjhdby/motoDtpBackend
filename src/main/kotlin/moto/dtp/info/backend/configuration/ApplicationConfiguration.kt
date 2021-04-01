package moto.dtp.info.backend.configuration

import com.mongodb.reactivestreams.client.MongoClient
import io.swagger.v3.oas.models.media.StringSchema
import moto.dtp.info.backend.repository.AccidentRepository
import moto.dtp.info.backend.repository.AuthRepository
import moto.dtp.info.backend.repository.UserRepository
import org.bson.types.ObjectId
import org.springdoc.core.SpringDocUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = [AccidentRepository::class, AuthRepository::class, UserRepository::class])
class ApplicationConfiguration {
    init {
        SpringDocUtils.getConfig().replaceWithSchema(ObjectId::class.java, StringSchema())
    }

    @Autowired
    private val mongoClient: MongoClient? = null

    @Bean
    fun reactiveMongoTemplate(): ReactiveMongoTemplate? {
        val template = ReactiveMongoTemplate(mongoClient!!, "motodtp")
        val converter = template.converter as MappingMongoConverter
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        converter.afterPropertiesSet()
        return template
    }
}