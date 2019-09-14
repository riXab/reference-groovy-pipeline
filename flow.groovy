def devQAStaging() {

	checkout scm 
	
	git url: "https://github.com/riXab/groovy-pipeline-scripting.git"
			
	def jdk = tool name: 'localJDK'
	env.JAVA_HOME = "${jdk}"
	withEnv(["PATH+MAVEN=${tool 'localMaven'}/bin"]) {
				bat 'mvn -o clean package'
	}	
    //env.PATH="${tool 'localMaven'}/bin:${env.PATH}"
    stage 'Dev'
    //bat 'mvn -o clean package'
    archive '**/*.war'

    stage 'QA'
	echo "Now, Staging"
    parallel(longerTests: {
		println "Starting Longer branch"
        runWithServer {
			println "Run with Server"
			url ->  //"https://github.com/riXab/groovy-pipeline-scripting.git"
            println "Executing btach command"
			bat "mvn -o -f sometests/pom.xml test -Durl=${url} -Dduration=30"
			println "DONE batch command"
        }
    }, quickerTests: {
		println "Starting Quicker branch"
        runWithServer {
			println "Run with Server"
			url -> //"https://github.com/riXab/groovy-pipeline-scripting.git"
            println "Executing btach command"
			bat "mvn -o -f sometests/pom.xml test -Durl=${url} -Dduration=20"
			println "DONE btach command"
        }
    })
    stage name: 'Staging', concurrency: 1
	echo "Starting Deployment for Staging.."
    deploy '**/*.war', 'staging'
	echo "Finished Deployment to Staging.."
}

def production() {
    input message: "Does http://localhost:8080/staging/ look good?"
    try {
        checkpoint('Before production')
    } catch (NoSuchMethodError _) {
        echo 'Checkpoint feature available in Jenkins Enterprise by CloudBees.'
    }
    stage name: 'Production', concurrency: 1
    node('master') {
        bat 'curl -I http://localhost:8080/staging/'
        unarchive mapping: ['**/*.war' : 'x.war']
        deploy 'x.war', 'production'
        echo 'Deployed to http://localhost:8080/production/'
    }
}

def deploy(war, id) {
	echo "start copying war file to tmp/webapps/ location"
	//println "start copying war file to tmp/webapps/ location"
	bat "mkdir tmp & cd tmp & mkdir webapps"
    bat "copy ${war} webapps"
	//bat "copy ${war} webapps/${id}.war"
	echo "finished copying war file to tmp/webapps/ location"
}

def undeploy(id) {
	echo "deleting war file from tmp/webapps/ location"
    bat "rm /tmp/webapps/${id}.war"
}

def runWithServer(body) {
    def id = UUID.randomUUID().toString()
    deploy '**/*.war', id
    try {
        body.call "http://localhost:8080/${id}/"
    } finally {
        undeploy id
    }
}

return this;
