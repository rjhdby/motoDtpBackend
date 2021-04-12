package moto.dtp.info.backend.domain

import org.bson.types.ObjectId

interface EntityWithId {
    val id: ObjectId?
}