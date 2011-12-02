require 'net/http'

CALATOLA=true
if CALATOLA then
	BASE_URI="calatola.man.poznan.pl"
	PORT=80
	APP_NAME="/rosrs4"
	ADMIN_LOGIN="wfadmin"
	ADMIN_PASSWORD="wfadmin!!!"
else 
	BASE_URI="localhost"
	PORT=8080
	APP_NAME=""
	ADMIN_LOGIN="wfadmin"
	ADMIN_PASSWORD="wfadmin!!!"
end

#CLIENT_NAME = "Wf4Ever myExperiment import tool"
#CLIENT_REDIRECTION_URI = "http://sandbox.wf4ever-project.org/import/home"
#CLIENT_NAME = "Wf4Ever myExperiment import tool - localhost"
#CLIENT_REDIRECTION_URI = "http://localhost:8080"
#CLIENT_NAME = "RO management tool"
#CLIENT_REDIRECTION_URI = "OOB"
CLIENT_NAME = "RODL portal - localhost"
CLIENT_REDIRECTION_URI = "http://localhost:8080/oauth"
#CLIENT_NAME = "RODL portal"
#CLIENT_REDIRECTION_URI = "http://sandbox.wf4ever-project.org/portal/oauth"
@clientId = "88ba791c-7581-4d40-a"

def createClient
	Net::HTTP.start(BASE_URI, PORT) {|http|
		req = Net::HTTP::Post.new(APP_NAME + '/clients')
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
		req.body = CLIENT_NAME + "
" + CLIENT_REDIRECTION_URI
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		s = response["location"]
		@clientId = (s.include?('/') ? s[(s.rindex('/')+1)..-1] : s).chomp
		puts "Client ID: " + @clientId
        puts "Code: " + response.code
    }
end
	

def getClientList
	Net::HTTP.start(BASE_URI, PORT) {|http|
		req = Net::HTTP::Get.new(APP_NAME + '/clients')
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD

		response = http.request(req)
		puts response.body
		puts "Code: " + response.code
    }
end


def deleteClient
	Net::HTTP.start(BASE_URI, PORT) {|http|
		req = Net::HTTP::Delete.new(APP_NAME + '/clients/' + @clientId)
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD

		response = http.request(req)
        puts "Code: " + response.code
    }
end

createClient
#getClientList
#deleteClient
