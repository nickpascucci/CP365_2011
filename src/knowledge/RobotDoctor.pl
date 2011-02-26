%RobotDoctor.pl: A Prolog program for diagnosing disease

diagnosis(Name, hemorrhoids) :- symptom(Name, itching), symptom(Name, irritation), symptom(Name, rectal_pain).
diagnosis(Name, asthma) :- symptom(Name, rapid_breathing), symptom(Name, sighing), symptom(Name, fatigue).
diagnosis(Name, mononucleosis) :- symptom(Name, fever), symptom(Name, sore_throat), symptom(Name, fatigue).
diagnosis(Name, gout) :- symptom(Name, pain_in_joint), symptom(Name, peeling_skin), symptom(Name, fever).
diagnosis(Name, diabetes) :- symptom(Name, thirst), symptom(Name, apetite), symptom(Name, nausea).

symptom(matthew, rapid_breathing).
symptom(matthew, sighing).
symptom(matthew, fatigue).

symptom(nick, fever).
symptom(nick, sore_throat).
symptom(nick, fatigue).
