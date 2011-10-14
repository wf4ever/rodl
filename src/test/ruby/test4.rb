require 'nokogiri'	
require 'net/http'
require 'choice'
require 'uuidtools'
require 'base64'
require 'zipruby'

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


WORKSPACE_ID = "testWorkspace"
USER_ID = "test-" + Base64.strict_encode64(UUIDTools::UUID.random_create().raw).tr("+/", "-_")
USER_ID_URL_SAFE = Base64.strict_encode64(USER_ID).tr("+/", "-_")
CLIENT_NAME = "ROSRS testing app written in Ruby"
CLIENT_REDIRECTION_URI = "OOB" # will not be used

RO_NAME="ro1"
VERSIONS={
	:ver1 => "ver1", 
	:ver2 => "ver2"
}

FILES={
	:manifest => { :name => "manifest.rdf", :dir => "", :path => "manifest.rdf" },
	:file1    => { :name => "file1.txt", :dir => "", :path => "file1.txt?a=b#foobar" },
	:file2    => { :name => "file2.txt", :dir => "dir/", :path => "dir/file2.txt" },
	:file3    => { :name => "file3.jpg", :dir => "testdir/", :path => "testdir/file3.jpg" }
}

MESSAGE_WIDTH=50
code = 200

INDEXING_TIME_INTERVAL = 30

URI_PREFIX_IN_MANIFEST = "URI_PREFIX"
if CALATOLA then
	URI_PREFIX = "http://calatola.man.poznan.pl/rosrs4/workspaces/" + WORKSPACE_ID
else
	URI_PREFIX = "http://localhost:8080/workspaces/" + WORKSPACE_ID
end

@retrievedManifest = ""
@accessToken = ""
@clientId = ""

def printResponse(response, expectedCode)
	printConstantWidth2(response.code + " " + response.message)
	if response.code.to_i == expectedCode
		puts " ok"
	else
		puts " failed"
	end
    if Choice.choices[:printHeaders]
      puts response.to_hash
    end   
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

def printConstantWidth2(message) 
	print message
	(25 - message.size).times {
		print " "
	}
end

def wait(seconds)
	print "Waiting #{seconds} seconds"
	seconds.times{ |i| 
		print "."
		sleep 1 
	}
	puts
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
	
	option :printHeaders do
    short '-H'
    long '--print-headers'
    desc 'If set, HTTP headers will be printed'
    default false
  end
end

def createWorkspace
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating workspace........"
		req = Net::HTTP::Post.new(APP_NAME + '/workspaces')
		req.add_field "Authorization", "Bearer " + @accessToken
		req.body = WORKSPACE_ID
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 201)
		code = response.code.to_i 
    }
end
	
def createUser
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating user........"
		req = Net::HTTP::Post.new(APP_NAME + '/users')
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
		req.body = USER_ID_URL_SAFE
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 201)
		code = response.code.to_i 
    }
end

def checkCreateUser
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating user again........"
		req = Net::HTTP::Post.new(APP_NAME + '/users')
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
		req.body = USER_ID_URL_SAFE
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 409)
		code = response.code.to_i 
    }
end

def createRO
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating research object........"
		req = Net::HTTP::Post.new(APP_NAME+ '/workspaces/' + WORKSPACE_ID + '/ROs')
		req.add_field "Authorization", "Bearer " + @accessToken
        req.body = RO_NAME
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 201)
		code = response.code.to_i 
    }
end

def createVersion(which = :ver1)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating version #{VERSIONS[which]}........"
		req = Net::HTTP::Post.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME)
		req.add_field "Authorization", "Bearer " + @accessToken
        req.body = VERSIONS[which]
        req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 201)
		code = response.code.to_i 
	}
end
		
def addFile(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Adding #{FILES[which][:name]}........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:path])
		req.add_field "Authorization", "Bearer " + @accessToken
		req.body = File.read(FILES[which][:name])
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 200)
		code = response.code.to_i 
	}
end

def getWorkspacesRdf
	#get list of research objects
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving list of workspaces........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/')
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		printResponse(response, 200)
	}
end

def getListRO
	#get list of research objects
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving list of research objects........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs')
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		printResponse(response, 200)
	}
end

def getROrdf
	#get research object rdf	
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving research object description........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME)
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		printResponse(response, 200)
	}
end
		
def getVersionZip(which = :ver1, expectedFiles = [ FILES[:manifest][:path] ])
	#get version zip
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving version #{VERSIONS[which]} archive........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[which] + '?content=true')
		req.add_field "Authorization", "Bearer " + @accessToken
		req.add_field "Accept", "application/zip"
		response = http.request(req)
		puts response.code + " " + response.message
		#no body printing -- binary file!
		if response.code.to_i == 200
			puts "Archive size: " + response["content-length"] if Choice.choices[:printBody]
		else
			puts response.body if Choice.choices[:printErrors]
		end
		if response.code.to_i == 200
    		Zip::Archive.open_buffer(response.body) do |ar|
                # Zip::Archive includes Enumerable
                entry_names = ar.map do |f|
                    if expectedFiles.include?(f.name)
                        expectedFiles.delete(f.name)
                    else
                        puts "                  Unexpected #{f.name}"
                    end
                end
                expectedFiles.each { |e| puts "                 File #{e} not found" }
            end
        end
	}	
end
	
def getManifest(which = :ver1)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving manifest........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[which])
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		@retrievedManifest = response.body
		printResponse(response, 200)
	}	
end

def assertElementExists(rdf, att, expected)
	if rdf.xpath("//#{att}").empty?
		puts "                         #{att} missing"
	else
		el = rdf.xpath("//#{att}").first
		if expected.nil?
			puts "                         #{att} blank value" if el.content.empty? and el.attribute("resource").nil?
		else
			puts "                         #{att} wrong value, expected #{expected} found #{el.content}" if el.content != expected and el.attribute("resource").nil?
		end
	end
end

def validateManifest1
	getManifest if @retrievedManifest.empty? or @retrievedManifest.nil?
	if @retrievedManifest.empty? or @retrievedManifest.nil?
		puts "Failed to retrieve manifest for validation"
		return
	end
	
	doc = Nokogiri::XML(@retrievedManifest)
	ns = doc.namespaces()
	if ns.size() != 4 or !ns.key?("xmlns:rdf") or !ns.key?("xmlns:ore") or !ns.key?("xmlns:dcterms") or !ns.key?("xmlns:oxds")
		puts "Wrong namespaces"
	end
	if doc.xpath("//rdf:Description").empty?
		puts "rdf:Description missing"
	else
		rdf = doc.xpath("//rdf:Description").first()
		assertElementExists(rdf, "dcterms:description", "")
		assertElementExists(rdf, "dcterms:title", "")
		assertElementExists(rdf, "dcterms:creator", "")
		assertElementExists(rdf, "dcterms:identifier", RO_NAME)
		assertElementExists(rdf, "dcterms:created", nil)
		assertElementExists(rdf, "dcterms:modified", nil)
		assertElementExists(rdf, "oxds:currentVersion", VERSIONS[:ver1])
	end
end
			
def validateManifest2
	getManifest if @retrievedManifest.empty? or @retrievedManifest.nil?
	if @retrievedManifest.empty? or @retrievedManifest.nil?
		puts "Failed to retrieve manifest for validation"
		return
	end
	
	doc = Nokogiri::XML(@retrievedManifest)
	ns = doc.namespaces()
	if ns.size() != 4 or !ns.key?("xmlns:rdf") or !ns.key?("xmlns:ore") or !ns.key?("xmlns:dcterms") or !ns.key?("xmlns:oxds")
		puts "Wrong namespaces"
	end
	if doc.xpath("//rdf:Description").empty?
		puts "rdf:Description missing"
	else
		rdf = doc.xpath("//rdf:Description").first()
		assertElementExists(rdf, "dcterms:description", "Description")
		assertElementExists(rdf, "dcterms:title", "Some title")
		assertElementExists(rdf, "dcterms:creator", "Wf4Ever test user")
		assertElementExists(rdf, "dcterms:identifier", RO_NAME)
		assertElementExists(rdf, "dcterms:created", nil)
		assertElementExists(rdf, "dcterms:modified", nil)
		assertElementExists(rdf, "oxds:currentVersion", VERSIONS[1])

		assertElementExists(rdf, "ore:aggregates", "")
	end
end
				
def getFileMetadata(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Retrieving #{FILES[which][:name]} metadata........"
			req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:path])
			req.add_field "Authorization", "Bearer " + @accessToken
			
			response = http.request(req)
			printResponse(response, 200)
	}	
end

def getFile(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Retrieving #{FILES[which][:name]} content........"
			req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:path] + '?content=true')
			req.add_field "Authorization", "Bearer " + @accessToken

			response = http.request(req)
			printResponse(response, 200)
	}	
end

def getDirectoryList(which)
	#get list of files in /dir
	Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Retrieving list of files in #{FILES[which][:dir]}........"
			req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:dir])
			req.add_field "Authorization", "Bearer " + @accessToken
			req.add_field "Accept", "application/xml+rdf"

			response = http.request(req)
			printResponse(response, 200)
	}	
end

def getDirectoryZipped(which)
	#get zipped files in /dir
	Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Retrieving zipped content of #{FILES[which][:dir]}........"
			req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:dir] + "?content=true")
			req.add_field "Authorization", "Bearer " + @accessToken
			req.add_field "Accept", "application/zip"

			response = http.request(req)
			printResponse(response, 200)
	}	
end

def updateFile(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Updating #{FILES[which][:name]}........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:path])
		req.add_field "Authorization", "Bearer " + @accessToken
		req.body = File.read(FILES[which][:name])
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 200)
	}
end

def updateManifest(version = 0)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Updating manifest........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1])
		req.add_field "Authorization", "Bearer " + @accessToken
		req.body = File.read("manifest.rdf").gsub(URI_PREFIX_IN_MANIFEST, URI_PREFIX)
		if version == 1 then req.body = req.body.gsub("Some title","New title") end
		req.add_field "Content-Type", "application/rdf+xml"

		response = http.request(req)
		printResponse(response, 200)
	}
end

def updateManifestMalformed
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Updating malformed manifest........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1])
		req.add_field "Authorization", "Bearer " + @accessToken
		req.body = File.read("manifest_malformed.rdf").gsub(URI_PREFIX_IN_MANIFEST, URI_PREFIX)
		req.add_field "Content-Type", "application/rdf+xml"

		response = http.request(req)
		printResponse(response, 400)
	}
end

def updateManifestIncorrect
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Updating incorrect manifest........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1])
		req.add_field "Authorization", "Bearer " + @accessToken
		req.body = File.read("manifest_incorrect.rdf").gsub(URI_PREFIX_IN_MANIFEST, URI_PREFIX)
		req.add_field "Content-Type", "application/rdf+xml"

		response = http.request(req)
		printResponse(response, 409)
	}
end

def createVersionAsCopy				
	#create version as a copy of another version
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating version basing on another........"
		req = Net::HTTP::Post.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME)
		req.body = VERSIONS[:ver2] + "
http://#{BASE_URI}:#{PORT.to_s}/#{APP_NAME}/workspaces/#{WORKSPACE_ID}/ROs/#{RO_NAME}/#{VERSIONS[:ver1]}"
		req.add_field "Content-Type", "text/plain"
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 201)
		
	}
end
				
def deleteFile(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting #{FILES[which][:name]}........"
		req = Net::HTTP::Delete.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:path])
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 204)
	}
end

def checkDeleteManifest(which = :ver1)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting manifest........"
		req = Net::HTTP::Delete.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[which] + '/manifest.rdf')
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 403)
	}
end
			
def deleteVersion(which = :ver1)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting version #{VERSIONS[which]}........"
		req = Net::HTTP::Delete.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[which])
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 204)
	}
end

def deleteRO
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting research object........"
		req = Net::HTTP::Delete.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME)
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 204)
	}		
end
	
def deleteWorkspace
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting workspace........"
		req = Net::HTTP::Delete.new(APP_NAME + '/workspaces/' + WORKSPACE_ID)
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		printResponse(response, 204)
	}
end

def deleteUser
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting user........"
		req = Net::HTTP::Delete.new(APP_NAME + '/users/' + USER_ID_URL_SAFE)
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
		response = http.request(req)
		printResponse(response, 204)
	}
end

def addEmptyDirectory(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating empty directory........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:dir])
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 200)
		code = response.code.to_i 
	}
end

def getDirectoryMetadata(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving directory metadata........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:dir])
		req.add_field "Authorization", "Bearer " + @accessToken
		
		response = http.request(req)
		printResponse(response, 200)
	}	
end

def deleteDirectory(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting directory........"
		req = Net::HTTP::Delete.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:dir])
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 204)
		code = response.code.to_i 
	}
end

def checkNoDirectory(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Retrieving empty directory metadata........"
			req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:dir])
			req.add_field "Authorization", "Bearer " + @accessToken
			
			response = http.request(req)
			printResponse(response, 404)
	}	
end

def checkNoFileMetadata(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
			printConstantWidth "Retrieving #{FILES[which][:name]} metadata........"
			req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:path])
			req.add_field "Authorization", "Bearer " + @accessToken
			
			response = http.request(req)
			printResponse(response, 404)
	}	
end

def checkNoFileContent(which)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving #{FILES[which][:name]} content........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[which][:path] + '?content=true')
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 404)
	}	
end

def createEdition
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating edition........"
		req = Net::HTTP::Post.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1])
		req.add_field "Authorization", "Bearer " + @accessToken
		req.add_field "Content-Type", "application/rdf+xml"

		response = http.request(req)
		printResponse(response, 201)
		code = response.code.to_i 
	}
end

def getFileEdition(whichFile, whichEdition)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving edition list........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '?edition_list')
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		@edition = response.body.split("\n")[whichEdition].split("=")[0]
		@edition.slice!(0) if @edition.chr == "*"
		printResponse(response, 200)
	}	
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Retrieving #{FILES[whichFile][:name]} content edition #{@edition}........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '/' + FILES[whichFile][:path] + '?content=true&edition_id=' + @edition)
		req.add_field "Authorization", "Bearer " + @accessToken

		response = http.request(req)
		printResponse(response, 200)
	}	
end

def publishEdition(which = :ver1)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Publishing version #{VERSIONS[which]}........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[which] + '?publish=true')
		req.add_field "Authorization", "Bearer " + @accessToken
		req.add_field "Content-Type", "application/rdf+xml" # leave?

		response = http.request(req)
		printResponse(response, 200)
	}
end

def unpublishEdition(which = :ver1)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Unpublishing version #{VERSIONS[which]}........"
		req = Net::HTTP::Put.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[which] + '?publish=false')
		req.add_field "Authorization", "Bearer " + @accessToken
		req.add_field "Content-Type", "application/rdf+xml" # leave?

		response = http.request(req)
		printResponse(response, 200)
	}
end

def checkPublished (which)
	Net::HTTP.start(BASE_URI, PORT) do |http|
		printConstantWidth "Checking #{which} is published........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs/' + RO_NAME + '/' + VERSIONS[:ver1] + '?edition_list')
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		printResponse(response, 200)

		i=0
		response.body.each_line do |s|
			edition = s.split("=")[0]
			edition.slice!(0) if edition.chr == "*"
			puts "Line #{i}: edition #{edition} should be published" if i == which && s.chr != "*"
			puts "Line #{i}: edition #{edition} should not be published" if i != which && s.chr == "*"
			i += 1
		end
	end	
end

def searchForROs(*expectedVersions)
	#get list of research objects
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Searching for research objects........"
		req = Net::HTTP::Get.new(APP_NAME + '/workspaces/' + WORKSPACE_ID + '/ROs?Creator=Wf4Ever+test+user&Title=Some+title')
		req.add_field "Authorization", "Bearer " + @accessToken
		response = http.request(req)
		printResponse(response, 200)
		
		received = Array.new
		response.body.each_line { |s| received << (s.include?('/') ? s[(s.rindex('/')+1)..-1] : s).chomp }
		expectedVersions.each do |v|
			if received.include?(VERSIONS[v]) then
				received.delete(VERSIONS[v])
			else
				puts "Version #{VERSIONS[v]} expected but not found"
			end
		end
		received.each { |r| puts "Received version #{r} that was not expected" }
	}
end


def createAccessToken
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating access token........"
		req = Net::HTTP::Post.new(APP_NAME + '/accesstoken')
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
		req.body = @clientId + "
" + USER_ID
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 201)
		s = response["location"]
		@accessToken = (s.include?('/') ? s[(s.rindex('/')+1)..-1] : s).chomp if !s.nil?
		code = response.code.to_i
    }
end
	

def getAccessTokenList (user = nil)
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Getting access token list........"
		queryparams = "user_id=#{user}" if user != nil
		req = Net::HTTP::Get.new(APP_NAME + "/accesstoken?#{queryparams}")
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD

		response = http.request(req)
		printResponse(response, 200)
		if !response.body.include?(@accessToken)
			puts "Access token missing"
		end
		code = response.code.to_i
    }
end


def deleteAccessToken
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting access token........"
		req = Net::HTTP::Delete.new(APP_NAME + '/accesstoken/' + @accessToken)
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD

		response = http.request(req)
		printResponse(response, 204)
		code = response.code.to_i
    }
end


def createClient
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Creating OAuth client........"
		req = Net::HTTP::Post.new(APP_NAME + '/clients')
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD
		req.body = CLIENT_NAME + "
" + CLIENT_REDIRECTION_URI
		req.add_field "Content-Type", "text/plain"

		response = http.request(req)
		printResponse(response, 201)
		s = response["location"]
		@clientId = (s.include?('/') ? s[(s.rindex('/')+1)..-1] : s).chomp
		code = response.code.to_i
    }
end
	

def getClientList
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Getting OAuth client list........"
		req = Net::HTTP::Get.new(APP_NAME + '/clients')
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD

		response = http.request(req)
		printResponse(response, 200)
		if !response.body.include?(@clientId)
			puts "Client id missing"
		end
		code = response.code.to_i
    }
end


def getClient
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Getting OAuth client data........"
		req = Net::HTTP::Get.new(APP_NAME + '/clients/' + @clientId)
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD

		response = http.request(req)
		printResponse(response, 200)
		if !response.body.include?(@clientId)
			puts "Client id missing"
		end
		code = response.code.to_i
    }
end


def deleteClient
	Net::HTTP.start(BASE_URI, PORT) {|http|
		printConstantWidth "Deleting OAuth client........"
		req = Net::HTTP::Delete.new(APP_NAME + '/clients/' + @clientId)
		req.basic_auth ADMIN_LOGIN, ADMIN_PASSWORD

		response = http.request(req)
		printResponse(response, 204)
		code = response.code.to_i
    }
end


if createClient == 201 && createUser == 201
    checkCreateUser
    getClientList
    getClient
    if createAccessToken == 201
	    getAccessTokenList
        if createWorkspace == 201
            getWorkspacesRdf
	        if createRO == 201
		        if createVersion == 201
			        getManifest
			        validateManifest1
			        if addFile(:file1) == 200 && addFile(:file2) == 200
#				        getListRO
#				        getROrdf
#				        getVersionZip :ver1, [ FILES[:manifest][:path], FILES[:file1][:path], FILES[:file2][:path] ]
#				        getManifest
#				        getFileMetadata(:file1)
#				        getFileMetadata(:file2)
#				        getFile(:file1)
#				        getFile(:file2)
#				        getDirectoryList(:file2)
#				        getDirectoryZipped(:file2)
#				        updateFile(:file1)
#				        updateFile(:file2)
#				        searchForROs
#				        updateManifest
#				        updateManifestMalformed
#				        updateManifestIncorrect
#				        publishEdition
#        #				wait INDEXING_TIME_INTERVAL
#        #				searchForROs(:ver1)
#				        createVersionAsCopy
#				        getManifest(:ver2)
#				        validateManifest2
#				        publishEdition(:ver2)
#        #				wait INDEXING_TIME_INTERVAL
#        #				searchForROs(:ver1, :ver2)
#				        updateManifest(1)
#        #				searchForROs(:ver2)
#				        unpublishEdition
#				        deleteFile(:file1)
#				        deleteFile(:file2)
#				        checkNoFileMetadata(:file1)
#				        checkNoFileContent(:file1)
#				        checkNoFileMetadata(:file2)
#				        checkNoFileContent(:file2)
#				        checkDeleteManifest
#			        end
#			        if addEmptyDirectory(:file2) == 200
#				        getDirectoryMetadata(:file2)
#				        addFile(:file2)
#				        getDirectoryMetadata(:file2)
#				        deleteFile(:file2)
#				        getDirectoryMetadata(:file2)
#				        deleteDirectory(:file2)
#				        checkNoDirectory(:file2)
#				        addFile(:file2)
#				        deleteDirectory(:file2)
#				        checkNoDirectory(:file2)
#			        end
#			        if addFile(:file1) == 200 && addFile(:file2) == 200 && createEdition == 201
#				        getFileEdition(:file1, 0)
#				        addFile(:file3)
#				        deleteFile(:file1)
#				        getFileMetadata(:file3)
#				        checkNoFileMetadata(:file1)
#				        checkNoFileContent(:file1)
#				        getFileEdition(:file1, 0)
#				        checkPublished -1
#				        publishEdition
#				        checkPublished 1
#				        if createEdition == 201
#					        deleteFile(:file2)
#					        checkNoFileMetadata(:file2)
#					        addFile(:file1)
#					        getFile(:file1)
#					        deleteFile(:file1)
#					        checkNoFileMetadata(:file1)
#					        checkNoFileContent(:file1)
#					        getFileEdition(:file1, 0)
#					        checkPublished 1
#					        publishEdition
#					        checkPublished 2
#					        unpublishEdition
#					        checkPublished -1
#				        end
			        end
			        deleteVersion
		        end
		        deleteRO
	        end
	        deleteWorkspace
        end
	    deleteAccessToken
    end
    deleteUser
    deleteClient
end
