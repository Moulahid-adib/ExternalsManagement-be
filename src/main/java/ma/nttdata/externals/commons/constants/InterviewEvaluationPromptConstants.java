package ma.nttdata.externals.commons.constants;

public final class InterviewEvaluationPromptConstants {

    private InterviewEvaluationPromptConstants(){}

    public static final String INTERVIEW_EVALUATION_PROMPT = """
            You're an expert interviewing manager and talent acquisition specialist.
            We've passed an interview for an #offer, to a #candidate, and we've gathered the information output and prepared a list of #Question/#answer from that interview,
            I will provide you below the needed information for them.
            Prepare a list of evaluations for that interview, each #evaluation_type is an entry in this list, I will also give you the list of #evaluation_types that we need to evaluate this candidate in.
                                       
            #Take in consideration these instructions:
             - The evaluations must be comprehensive and fair considering the job requirements in #offer_data and the #candidate_data
             - In relevance to the #evaluation_type being assessed, look in the #answers for technical accuracy, depth of knowledge, problem-solving approaches, and communication skills.
             - If the #answer is correct and the time of #answer is lower than the time given in the question, take it into account for positive assessment.
             - Cross-reference #candidate answers with #job requirements to ensure role-specific #evaluation.
             - Take evaluation type #coefficients in consideration
             - Ensure fairness by matching evaluation difficulty and accuracy to candidate's stated experience level
             - Use the exact 'description' value from each EvaluationTypes list for the 'evaluationTypeDescription' field in the output.
             - Ensure each evaluation in the output array corresponds exactly to one EvaluationType from the input list.
             - Do not skip any evaluation types or add additional ones not provided in the Evaluation Types.
             - Reference specific technologies, skills, or experiences mentioned in the candidate profile when relevant.
             - Use simple language: A2-B1-B2
             - Return ONLY a valid JSON array with exactly this structure, no additional text or formatting:"{JSON_SCHEMA}", here you have a mock example:"{JSON_MOCK}".
                                       
            I provide bellow the needed information:
             - #Candidate Profile: "{CANDIDATE_DATA}",
             - #Job Offer requirements: "{OFFER_DATA}",
             - #Evaluation Types criteria: "{EVALUATION_TYPES_DATA}".
             - #Questions And Answers with the estimated answer time and the real answer time: "{QuestionAnswer_DATA}"
            """;

    public static final String JSON_SCHEMA = """
            [
              {
                "score": "Double - between 0.00 and 100.00",
                "feedback": "String - Try to give an overall feedback of the performance of the candidate in this evaluation type",
                "evaluationTypeDescription": "String - use the exact 'description' field value from the corresponding EvaluationType entity"
              }
            ]
            """;

    public static final String JSON_MOCK = """
        [
          {
             "score": 78,
             "feedback": "The candidate demonstrated solid problem-solving skills by breaking down complex scenarios into manageable steps. They showed logical thinking when approaching the algorithm challenge, though they took 35% longer than estimated time (12 minutes vs 8 minutes expected). Their approach was methodical but could benefit from faster pattern recognition to improve efficiency in time-constrained situations.",
             "evaluationTypeDescription": "Problem Solving"
           },
           {
             "score": 82,
             "feedback": "Communication was clear and well-structured throughout the interview. The candidate articulated technical concepts effectively and asked relevant clarifying questions. They maintained good eye contact and professional demeanor. Response time was generally within expected ranges. Minor improvement area: occasionally used too much technical jargon when simpler explanations would suffice for broader team communication.",
             "evaluationTypeDescription": "Communication"
           },
           {
             "score": 88,
             "feedback": "The candidate demonstrated strong technical expertise, accurately answering most technical questions and applying relevant frameworks and tools to proposed solutions. They showcased up-to-date knowledge in backend development and database optimization. A slight gap was noted in the depth of cloud deployment strategies.",
             "evaluationTypeDescription": "Technical Knowledge"
           },
           {
             "score": 74,
             "feedback": "Teamwork skills appear solid, with examples of successful collaboration and conflict resolution in previous roles. The candidate emphasizes open communication and adaptability when working with different personalities. Could improve by actively seeking feedback from peers during collaborative tasks.",
             "evaluationTypeDescription": "Teamwork"
           },
           {
             "score": 91,
             "feedback": "The candidate showed high adaptability when presented with unexpected problem changes. They adjusted their approach quickly and kept focus on delivering a workable solution without losing quality. This flexibility is valuable in dynamic project environments.",
             "evaluationTypeDescription": "Adaptability"
           },
           {
             "score": 69,
             "feedback": "Time management was acceptable but could be optimized. While the candidate met deadlines for most answers, they occasionally exceeded the suggested time limit in more complex questions, which may affect performance under strict delivery timelines.",
             "evaluationTypeDescription": "Time Management"
           }
        ]
        """;


    public static final String INTERVIEW_EVALUATION_PROMPT_CODE = "INTERVIEW_EVALUATION_PROMPT";

    public static final String OFFER_PLACEHOLDER = "{OFFER_DATA}";
    public static final String CANDIDATE_PLACEHOLDER = "{CANDIDATE_DATA}";
    public static final String QUESTION_ANSWER_DTO_PLACEHOLDER = "{QuestionAnswer_DATA}";
    public static final String EVALUATION_TYPES_PLACEHOLDER = "{EVALUATION_TYPES_DATA}";
    public static final String JSON_SCHEMA_PLACEHOLDER = "{JSON_SCHEMA}";
    public static final String JSON_MOCK_PLACEHOLDER = "{JSON_MOCK}";
}