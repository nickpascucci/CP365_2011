# BUG 2
# By Andrew and Nick

from livewires import games, color
import random
from Goal import *
from Bug import *
import math

# Environmental inputs:
#   Am I bumping something?  self.bumping boolean
#   Where is the goal?       self.goal.x, self.goal.y

# Choose a next move by returning deltas for x and y
#   Move down and to the right:  (1, 1)
#   Move directly left:          (-1, 0)
#   Move up:                     (0, -1)
#   Go crazy:                    (1-2*random.random(), 1-2*random.random())

class MyBug(Bug):

	def setup(self):
		self.direction = (1, 1)
		self.timeSinceBump = 0
		self.following = False
		self.vectorToGoal = self.get_vector_to_goal()
		self.movesFollowed = 0

	def choose_move(self):
		move = self.get_move()
		#print "Move: " , move, " position: ", self.x, "," , self.y
		return move

	def get_move(self):
		#Move towards goal until you bump
		if self.following:
			#REAL WORK GOES HERE
			#DAMN YOU, COLONS
			
			if self.check_vector() and self.movesFollowed > 100:
				print "On vector! Wheee!"
				self.direction = self.get_1_1_vector_to_goal()
				self.following = False
				self.movesFollowed = 0
				return self.direction
			self.movesFollowed += 1

		if not self.following and not self.bumping:
			self.direction = self.get_1_1_vector_to_goal()
			return self.direction
		elif self.bumping and not self.following:
			print "Going into follow mode."
		#Go into follow mode upon bump
			self.firstBump = (self.x, self.y)
			self.following = True
			self.timeSinceBump = 0
		if self.following and not self.bumping:
			#Move until bump or timeout
			if self.timeSinceBump > 10:
				print "Timeout, turning right."
				self.turn_right()
				self.timeSinceBump = 0
			else:
				print "Trusting myself to follow the wall."
				self.timeSinceBump += 1
			return self.direction
		elif self.bumping and self.following:
			"Found a wall again."
			self.turn_left()
			self.timeSinceBump = 0
			return self.direction

	#Turns counterclockwise, to nearest right angle
	def turn_left(self):
		if self.direction == (1, 1) or self.direction == (0, 1):
			self.direction = (1, 0)
		elif self.direction == (1, -1) or self.direction == (1, 0):
			self.direction = (0, -1)
		elif self.direction == (-1, -1) or self.direction == (0, -1):
			self.direction = (-1, 0)
		else:
			self.direction = (0, 1)
	
	#Turns clockwise, to nearest right angle. Only works if you're already
	#moving at right angles.
	def turn_right(self):
		if self.direction == (0, 1):
			self.direction = (-1, 0)
		elif self.direction == (1, 0):
			self.direction = (0, 1)
		elif self.direction == (0, -1):
			self.direction = (1, 0)
		else:
			self.direction = (0, -1)

	def check_vector(self):
		vec1 = self.get_vector_to_goal()
		errX = vec1[0] - self.vectorToGoal[0]
		errY = vec1[1] - self.vectorToGoal[1]
		error = math.fabs(errX) + math.fabs(errY)
		if error < .1:
			return True
		else:
			return False

	#Gets a vector to the goal?
	def get_vector_to_goal(self):
			x = self.goal.x - self.x
			y = self.goal.y - self.y
			magnitude = math.sqrt(x**2 + y**2)
			x /= magnitude
			y /= magnitude
			
			return (x*5, y*5)

	def get_1_1_vector_to_goal(self):
		x = self.goal.x - self.x
		y = self.goal.y - self.y
		if x > 0:
			x = 1
		elif x < 0:
			x = -1
		else:
			x = 0
		
		if y > 0:
			y = 1
		if y < 0:
			y = -1
		else:
			y = 1
		return (x, y)
