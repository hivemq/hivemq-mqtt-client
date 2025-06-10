rootProject.name = "hivemq-mqtt-client"

for (module in listOf("websocket", "proxy", "epoll", "reactor", "examples")) {
    include("${rootProject.name}-$module")
    project(":${rootProject.name}-$module").projectDir = file(module)
}
