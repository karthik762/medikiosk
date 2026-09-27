# MediKiosk — Reference Datasets & Evaluation Fixtures

## 1. Overview
MediKiosk uses open research datasets solely as **design references** and **evaluation benchmark structures**. Raw copyrighted or massive external dataset dumps are strictly prohibited in source control.

## 2. Reference Benchmarks

### 1. Synthea (Synthetic Patient Histories)
- **Role**: Reference structure for synthetic patient demographic profiles, chronic conditions, medication lists, and FHIR observations.
- **Implementation**: Used to structure local unit test fixtures and mock clinical states without downloading external binaries.

### 2. MTS-Dialog (Clinical Dialogue Corpus)
- **Role**: Reference structure for multi-turn doctor-patient conversations (HPI, PMH, ROS, summaries).
- **Implementation**: Informs `MultiTurnEvaluationTest` multi-turn dialogue progression (Chest Pain, Abdominal Pain, Negation, Contradiction).

### 3. MedQuAD (Medical Question Answering)
- **Role**: Reference for natural, patient-friendly phrasing of clinical questions.
- **Implementation**: Informs template wording in `QuestionIntent` and safety prompt constraints in `OpenAiCompatibleAiModelGateway`.

## 3. Data Governance & Privacy
- **Zero Raw External Data**: No third-party clinical datasets are stored in Git.
- **Synthetic Fixtures Only**: All test data in `src/test/java` is synthetically created.
- **Data Minimization**: Patient statements and voice audio remain ephemeral.
