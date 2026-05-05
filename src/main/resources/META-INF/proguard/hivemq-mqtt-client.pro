#
# Copyright 2018-present HiveMQ and the HiveMQ Community
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# javax.naming.ldap is referenced by Netty's EnhancingX509ExtendedTrustManager
# (since Netty 4.1.131) to extract the Common Name from X.509 certificates
# when enhancing TLS handshake error messages. These classes are part of the
# JDK's java.naming module but are not available on Android, causing R8 to
# fail with "Missing class" errors during minification.
-dontwarn javax.naming.ldap.**
