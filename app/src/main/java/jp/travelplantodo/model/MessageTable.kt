package jp.travelplantodo.model

import java.io.Serializable
import java.sql.Timestamp


class MessageTable(val message: String,val groupId: String, val userUid: String, val userName: String, val createdAt: String) : Serializable  {



}