package hisnet.login

class HisnetLoginFailException(
    statusCode: Int,
    description: String
) : MileageException("Status Code: $statusCode, Description: $description")
