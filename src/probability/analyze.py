'''
 Analyzer for text files
 Tracks the occurrences of words in blogs and splogs
 By Matt Polk and Nick Pascucci
 - Run 'sort' before this script to generate the list of files
 - This script doesn't really classify the blogs/splogs well;
   it chokes on a division by zero. My guess? We're running off
   the bottom of the float representation in multiplying
   our rhs in get_spam_probability_file and its counterpart.
'''
import operator

'''
 Implements our Bayesian analysis
 Takes a filename and dictionary of splogWords
 Returns a normalized tuple (prob_is_spam, prob_is_ham)
'''
def evaluate_file(filename, prob_spam, splogWords, blogWords):
	probFileIsSpam = float(get_spam_probability_file(filename, prob_spam, splogWords))
	probFileIsHam = float(get_ham_probability_file(filename, 1-prob_spam, blogWords))
	print "Probability that", filename, "is spam:", probFileIsSpam, "probability is ham:", probFileIsHam
	z = probFileIsSpam + probFileIsHam
	probFileIsSpam /= z
	probFileIsHam /= z
	return (probFileIsSpam, probFileIsHam)

'''
 Checks to see if a file looks like spam.
'''
def looks_like_spam(filename, prob_spam, splog_words, blog_words):
	probs = evaluate_file(filename, prob_spam, splog_words, blog_words)
	if probs[0] > probs[1]:
		return True
	else:
		return False

'''
 Calculates the probability that the file is spam.
 Returns a non-normalized value.
'''
def get_spam_probability_file(filename, prob_spam, splogWords):
	fileWords = get_words(filename)
	rhs = prob_spam
	for word in fileWords:
		if word in splogWords and splogWords[word] != 0:
			rhs *= splogWords[word]
	return rhs

'''
 Calculates the probability that the file is not spam.
 Returns a non-normalized value.
'''
def get_ham_probability_file(filename, prob_ham, blogWords):
	fileWords = get_words(filename)
	rhs = prob_ham
	for word in fileWords:
		if word in blogWords and blogWords[word] != 0:
			rhs *= blogWords[word]
	return rhs

'''
 Calculates the probability of a word being in a spam blog.
 Returns P(Spam | Word)
'''
def get_spam_probability_word(word, numSplogs, numBlogs, splogWords, blogWords):
	numSplogs = float(numSplogs)
	numBlogs = float(numBlogs)
	if not word in blogWords:
		return 1
	numerator = splogWords[word]*(numSplogs/(numSplogs+numBlogs))
	denominator = numerator + blogWords[word]*(numBlogs/(numBlogs+numSplogs))
	return numerator/denominator

'''
 Grabs all unique words from a file, returns a list of them
'''
def get_words(filename):
	file = open(filename, 'r')
	fileWords = []
	for line in file:
		words = line.split()
		for word in words:
			word = word.lower().strip(" -.?!#$@%^&*(){}<>|\"'")
			if word == '' or word in fileWords: # Skip multiple occurrences of a word in each file
				continue
			fileWords.append(word)
	return fileWords

'''
 Main program sequence. Analyzes a directory
 of files, taking filenames to be analyzed from trimmed,
 and calculates the spammiest words.
'''
def main():
	print "Splog/Blog Analyzer \n by Matt Polk and Nick Pascucci\n"
	print "Analyzing files..."
	blogs = []
	splogs = []

	#Create a listing of files to be analyzed
	filenames = open('trimmed', 'r')
	line = filenames.readline()
	while line:
		tokens = line.strip().split()
		if tokens[1] == "1":
			name = tokens[0].split('/')[1] #Make sure we get only the filename, not dir
			splogs.append(name)
		else:
			name = tokens[0].split('/')[1]
			blogs.append(name)
		line = filenames.readline()

	# Iterate over the splog list, add words to our dictionary or increment their value
	splogWords = {}
	for splog in splogs:
		file = open(splog, 'r')
		fileWords = {}
		for line in file:
			words = line.split()
			for word in words:
				word = word.lower().strip(" -.?!#$@%^&*(){}<>|\"'")
				if word == '' or word in fileWords: # Skip multiple occurrences of a word in each file
					continue
				if not word in splogWords: # If the word isn't in our dictionary
					splogWords[word] = 1 # Add it
				else:
					splogWords[word] += 1 # Otherwise, increment its count
				fileWords[word] = 1
		fileWords.clear()

	# Same for the blog list.
	blogWords = {}
	for blog in blogs:
		file = open(blog, 'r')
		fileWords = {}
		for line in file:
			words = line.split()
			for word in words:
				word = word.lower().strip(" -.?!#$@%^&*(){}<>|\"'")
				if word == '' or word in fileWords: # Skip multiple occurrences of a word in each file
					continue
				if not word in blogWords: # If the word isn't in our dictionary
					blogWords[word] = 1 # Add it
				else:
					blogWords[word] += 1 # Otherwise, increment its count
				fileWords[word] = 1 # Add it to our file based dictionary.
		fileWords.clear()

	print "Splogs had a total of ", len(splogWords), " unique words."
	print "Blogs had a total of ", len(blogWords), " unique words."

	# Calculate ratios of occurrences of words
	for word in blogWords:
		blogWords[word] /= float(len(blogs))

	for word in splogWords:
		splogWords[word] /= float(len(splogs))

	spammiestWords = {}
	# Finds the spammiest words. Uses get_spam_probability_word:
	# get_spam_probability(word, numSplogs, numBlogs, splogWords, blogWords)
	for word in splogWords:
		spammiestWords[word] = get_spam_probability_word(word, len(splogs), len(blogs), splogWords, blogWords)

	# Modified this code from http://stackoverflow.com/questions/613183/python-sort-a-dictionary-by-value
	# It sorts the dictionary and then prints out the top 25 spammy words.
	sorted_spam_words = sorted(spammiestWords.iteritems(), key=operator.itemgetter(1), reverse=True)
	print "Most spammy words:"
	for i in range(0, 25):
		print '{0}: {1}'.format(i+1, sorted_spam_words[i])

#The below is commented out because it kills the script.
'''
	# def looks_like_spam(filename, prob_spam, splog_words, blog_words):
	correct_spam = 0
	incorrect_spam = 0	
	prob_spam = float(len(splogs))/(float(len(splogs))+float(len(blogs)))
	print "Probability a file is spam:", prob_spam
	for target in splogs:
		if looks_like_spam(target, prob_spam, splogWords, blogWords):
			correct_spam += 1
		else:
			incorrect_spam += 1

	for target in blogs:
			if not looks_like_spam(target, prob_spam, splogWords, blogWords):
				correct_spam += 1
			else:
				incorrect_spam += 1
	print "Tagged", correct_spam + incorrect_spam, "pages as spam.", correct_spam, "pages were correct, and", incorrect_spam, "pages were incorrect."
'''


# Takes us to main if the file is executed directly
if __name__ == "__main__":
	main()

