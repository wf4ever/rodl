require 'net/http'
require 'choice'

#BASE_URI="ivy.man.poznan.pl"
#PORT=80
#APP_NAME="rosrs"

BASE_URI="localhost"
PORT=8081
APP_NAME="ro-srs"

#ADMIN_LOGIN="wfadmin"
#ADMIN_PASSWORD="wfadmin!!!"
ADMIN_LOGIN="admin"
ADMIN_PASSWORD="admin"

WORKSPACE_ID="rubyUser"
PASSWORD="pass"

RO_NAME="ro1"
VERSION_NAME="ver1"
VERSION_2_NAME="ver2"

FILE1_NAME="file1.txt"
FILE1_PATH="file1.txt"

FILE2_NAME="file2.txt"
FILE2_PATH="dir/file2.txt"

MESSAGE_WIDTH=50
code = 200

def printResponse(response, expectedCode)
		puts response.code + " " + response.message
		if response.code.to_i == expectedCode
			puts response.body if Choice.choices[:printBody] and expectedCode != 204#NO CONTENT
		else
			puts response.body if Choice.choices[:printErrors]
		end
end

def printConstantWidth(message) 
	print message
	(MESSAGE_WIDTH - message.size).times {
		print "."
	}
end

Choice.options do
	header 'Options:'
	
	option :printBody do
		short '-p'
		long '--print-body'
		desc 'If set, bodies of correct responses will be printed'
		default false
	end
	
	option :printErrors do
		short '-e'
		long '--print-errors'
		desc 'If set, error messages will be printed'
		default false
	end
end


#create workspace
Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating workspace........"
		req = Net::HTTP::Put.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID)
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
		req.body = PASSWORD
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 201)
		code = response.code.to_i 
    }
	
if code == 201
	#create ro
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating research object........"
		req = Net::HTTP::Put.new('/' + APP_NAME+ '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME)
		req.basic_auth WORKSPACE_ID, PASSWORD

		response = http.request(req)
		code = response.code.to_i 
		printResponse(response, 201)
		
    }

	if code == 201
		#create version
		Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Creating version........"
			req = Net::HTTP::Post.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME)
			req.basic_auth WORKSPACE_ID, PASSWORD

			response = http.request(req)
			printResponse(response, 201)
			code = response.code.to_i 
		}
		
		if code == 201
			#add file1
			code1 = 200
			Net::HTTP.start(BASE_URI, PORT) {|http|
				printConstantWidth "Adding file1........"
				req = Net::HTTP::Post.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE1_PATH)
				req.basic_auth WORKSPACE_ID, PASSWORD
				req.body = File.read(FILE1_NAME)
				req.add_field "Content-Type", "text/plain"

				response = http.request(req)
				printResponse(response, 200)
				code1 = response.code.to_i 
			}

			#add file2
			Net::HTTP.start(BASE_URI, PORT) {|http|
				printConstantWidth "Adding file2........"
				req = Net::HTTP::Post.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE2_PATH)
				req.basic_auth WORKSPACE_ID, PASSWORD
				req.body = File.read(FILE2_NAME)
				req.add_field "Content-Type", "text/plain"

				response = http.request(req)
				printResponse(response, 200)
				code = response.code.to_i 
			}
			
			code = (code + code1) /2 
		
			if code == 200
				#get list of research objects
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving list of research objects........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs')
						req.basic_auth WORKSPACE_ID, PASSWORD
						response = http.request(req)
						printResponse(response, 200)
					}

				#get research object rdf	
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving research object description........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME)
						req.basic_auth WORKSPACE_ID, PASSWORD
						response = http.request(req)
						printResponse(response, 200)
					}	
		
				#get version rdf
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving version description........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME)
						req.basic_auth WORKSPACE_ID, PASSWORD
						req.add_field "Accept", "application/rdf+xml"
						response = http.request(req)
						printResponse(response, 200)
					}	
				#get version zip
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving version archive........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME)
						req.basic_auth WORKSPACE_ID, PASSWORD
						req.add_field "Accept", "application/zip"
						response = http.request(req)
						puts response.code + " " + response.message
						#no body printing -- binary file!
						if response.code.to_i == 200
							puts "Archive size: " + response["content-length"] if Choice.choices[:printBody]
						else
							puts response.body if Choice.choices[:printErrors]
						end
						
					}	
					
				#get manifest
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving manifest........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + "/manifest.rdf")
						req.basic_auth WORKSPACE_ID, PASSWORD
						response = http.request(req)
						printResponse(response, 200)
					}	
				
				#get file1 metadata				
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving file1 metadata........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE1_PATH)
						req.basic_auth WORKSPACE_ID, PASSWORD
						
						response = http.request(req)
						printResponse(response, 200)
				}	
				#get file2 metadata
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving file2 metadata........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE2_PATH)
						req.basic_auth WORKSPACE_ID, PASSWORD
						
						response = http.request(req)
						printResponse(response, 200)
				}	

				#get file1
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving file1 content........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE1_PATH + '?content=true')
						req.basic_auth WORKSPACE_ID, PASSWORD

						response = http.request(req)
						printResponse(response, 200)
				}	
				#get file2
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Retrieving file2 content........"
						req = Net::HTTP::Get.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE2_PATH + '?content=true')
						req.basic_auth WORKSPACE_ID, PASSWORD

						response = http.request(req)
						printResponse(response, 200)
				}	

				#update file1
				Net::HTTP.start(BASE_URI, PORT) {|http|
					printConstantWidth "Updating file1........"
					req = Net::HTTP::Post.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE1_PATH)
					req.basic_auth WORKSPACE_ID, PASSWORD
					req.body = File.read(FILE1_NAME)
					req.add_field "Content-Type", "text/plain"

					response = http.request(req)
					printResponse(response, 200)
					
				}


				#update file2
				Net::HTTP.start(BASE_URI, PORT) {|http|
					printConstantWidth "Updating file2........"
					req = Net::HTTP::Post.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE2_PATH)
					req.basic_auth WORKSPACE_ID, PASSWORD
					req.body = File.read(FILE2_NAME)
					req.add_field "Content-Type", "text/plain"

					response = http.request(req)
					printResponse(response, 200)
				}
				
				#update manifest
				Net::HTTP.start(BASE_URI, PORT) {|http|
					printConstantWidth "Updating manifest........"
					req = Net::HTTP::Post.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE2_PATH)
					req.basic_auth WORKSPACE_ID, PASSWORD
					req.body = File.read("manifest.rdf")
					req.add_field "Content-Type", "application/rdf+xml"

					response = http.request(req)
					printResponse(response, 200)
				}
				
				#create version as a copy of another version
				Net::HTTP.start(BASE_URI, PORT) {|http|
					printConstantWidth "Creating version basing on another........"
					req = Net::HTTP::Post.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_2_NAME)
					req.body = "http://" + BASE_URI + ':' + PORT.to_s + '/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME
					req.add_field "Content-Type", "text/plain"
					req.basic_auth WORKSPACE_ID, PASSWORD

					response = http.request(req)
					printResponse(response, 201)
					
				}
				
				#delete file1
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Deleting file1........"
						req = Net::HTTP::Delete.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE1_PATH)
						req.basic_auth WORKSPACE_ID, PASSWORD

						response = http.request(req)
						printResponse(response, 204)

					}
				#delete file2
				Net::HTTP.start(BASE_URI, PORT) {|http|
						printConstantWidth "Deleting file2........"
						req = Net::HTTP::Delete.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME + '/' + FILE2_PATH)
						req.basic_auth WORKSPACE_ID, PASSWORD

						response = http.request(req)
						printResponse(response, 204)
					}
			end
			
			#delete version
			Net::HTTP.start(BASE_URI, PORT) {|http|
				printConstantWidth "Deleting version........"
				req = Net::HTTP::Delete.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSION_NAME)
				req.basic_auth WORKSPACE_ID, PASSWORD

				response = http.request(req)
				printResponse(response, 204)
			}
		end

		#delete ro
		Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Deleting research object........"
			req = Net::HTTP::Delete.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME)
			req.basic_auth WORKSPACE_ID, PASSWORD

			response = http.request(req)
			printResponse(response, 204)
		}		
	end
	
	#delete workspace
	Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Deleting workspace........"
			req = Net::HTTP::Delete.new('/' + APP_NAME + '/workspaces/' + WORKSPACE_ID)
			req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
			response = http.request(req)
			printResponse(response, 204)
		}
end 


	



