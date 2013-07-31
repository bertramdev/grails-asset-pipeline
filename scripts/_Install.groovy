def assetsFile = new File(basedir, 'grails-app/assets')
if (!new assetsFile.exists()) {
	assetsFile.mkdirs()
}

def javascriptsFile = new File(basedir, 'grails-app/assets/javascripts')
if (!javascriptsFile.exists()) {
	javascriptsFile.mkdirs()
}

def stylesheetsFile = new File(basedir, 'grails-app/assets/stylesheets')
if (!stylesheetsFile.exists()) {
	stylesheetsFile.mkdirs()
}

def imagesFile = new File(basedir, 'grails-app/assets/images')
if (!imagesFile.exists()) {
	imagesFile.mkdirs()
}

// TODO: Create Templated stylesheet and javascript file
