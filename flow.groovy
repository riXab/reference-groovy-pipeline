def devQAStaging() {

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

    parallel(longerTests: {
        runWithServer {url ->  //"https://github.com/riXab/groovy-pipeline-scripting.git"
            bat "mvn -o -f sometests/pom.xml test -Durl=${url} -Dduration=30"
        }
    }, quickerTests: {
        runWithServer {url -> //"https://github.com/riXab/groovy-pipeline-scripting.git"
            bat "mvn -o -f sometests/pom.xml test -Durl=${url} -Dduration=20"
        }
    })
    stage name: 'Staging', concurrency: 1
    deploy '**/*.war', 'staging'
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
    bat "cp ${war} /tmp/webapps/${id}.war"
}

def undeploy(id) {
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
