package com.realityexpander.spotifyclone.common

data class Resource<out T>(val status: Status, val payload: T?, val message: String?) {

    companion object {
        fun <T> success(payload: T?) = Resource(Status.SUCCESS, payload, null)

        fun <T> error(message: String, payload: T?) = Resource(Status.ERROR, payload, message)

        fun <T> loading(payload: T?) = Resource(Status.LOADING, payload, null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}